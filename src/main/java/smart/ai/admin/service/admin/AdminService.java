package smart.ai.admin.service.admin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import smart.ai.admin.service.common.CommandService;

@Service
public class AdminService {
    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    CommandService commandService;

    public Flux<ServerSentEvent<String>> startService(String hostName, String target, String num) {
        String startDate = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
        String endDate = (new SimpleDateFormat("yyyy")).format(new Date()) + "1231";
        String command = String.format("cd /smartai/deploy/shell && sh start.daemon%1$s.%2$s.sh && sh start.daemon.sh daemon%1$s %2$s %3$s %4$s", num, target, startDate, endDate);
        return commandService.streamCommand(hostName, command);
    }

    public Flux<ServerSentEvent<String>> stopService(String hostName, String target, String num) {
        String command = String.format("cd /smartai/deploy/shell && sh stop.daemon%1$s.%2$s.sh", num, target);
        return commandService.streamCommand(hostName, command);
    }

    public Flux<ServerSentEvent<String>> updateService(String hostName, String target, String num, String cnt) {
        String command = String.format("cd /smartai/deploy/shell && sh update.sh daemon%1$s %2$s %3$s", num, target, cnt);
        return commandService.streamCommand(hostName, command);
    }

    public Flux<ServerSentEvent<String>> getStatus(Map<String, Object> req) {
        String format = "cd /smartai/deploy/shell && sh status.daemon%2$s.%1$s.sh";
        return callApiAsync(format, req);
    }

    public Flux<ServerSentEvent<String>> getThreadCnt(Map<String, Object> req) {
        String format = "cd /smartai/deploy/shell && sh thread.cnt.sh daemon%2$s %1$s";
        return callApiAsync(format, req);
    }

    @SuppressWarnings("unchecked")
    private Flux<ServerSentEvent<String>> callApiAsync(String format, Map<String, Object> req) {
        Map<String, List<List<Object>>> reqMap = new HashMap<>();
        try {
            for (Map.Entry<String, Object> entry : req.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof List<?>) {
                    List<?> outerList = (List<?>) value;
                    if (outerList.stream().allMatch(item -> item instanceof List<?>)) {
                        reqMap.put(key, (List<List<Object>>) value);
                    } else {
                        return Flux.error(new IllegalArgumentException("Failed to parse input"));
                    }
                } else {
                    return Flux.error(new IllegalArgumentException("Failed to parse input"));
                }
            }
        } catch (ClassCastException e) {
            return Flux.error(new IllegalArgumentException("Failed to parse input"));
        }

        return Flux.fromIterable(reqMap.entrySet())
                .flatMap(entry -> {
                    String hostName = entry.getKey();
                    List<List<Object>> targets = entry.getValue();

                    return Flux.fromIterable(targets)
                            .flatMap(targetInfo -> {
                                String target = targetInfo.get(0).toString();
                                String num = targetInfo.get(1).toString();
                                String command = String.format(format, target, num);
                                return commandService.executeCommand(hostName, command)
                                        .flatMap(res -> {
                                            try {
                                                JsonNode node = objectMapper.readTree(res);
                                                Map<String, Object> responseMap = new HashMap<>();
                                                responseMap.put("status", node.get("status").asInt());
                                                responseMap.put("host", hostName);
                                                responseMap.put("target", target);
                                                responseMap.put("num", num);
                                                responseMap.put("body", node.get("body").asInt());
                                                return Mono.just(ServerSentEvent.<String>builder().data(objectMapper.writeValueAsString(responseMap)).build());
                                            } catch (JsonProcessingException e) {
                                                return Mono
                                                        .error(new RuntimeException("Error converting Map to JSON", e));
                                            }
                                        })
                                        .onErrorResume(e -> {
                                            Map<String, Object> resMap = new HashMap<>();
                                            resMap.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
                                            resMap.put("host", hostName);
                                            resMap.put("target", target);
                                            resMap.put("num", num);
                                            try {
                                                return Mono.just(ServerSentEvent.<String>builder().data(objectMapper.writeValueAsString(resMap)).build());
                                            } catch (JsonProcessingException ex) {
                                                return Mono
                                                        .error(new RuntimeException("Error handling JSON error", ex));
                                            }
                                        });
                            });
                });
    }
}