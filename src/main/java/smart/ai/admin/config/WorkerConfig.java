package smart.ai.admin.config;

// TODO: V2 개발 완료 후 기존 Worker 설정 복원 필요
// 기존 외부 Worker 설정 클래스 - V2 개발 중 임시 주석 처리

/*
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import smart.ai.admin.domain.WorkerData;

@Configuration
public class WorkerConfig {
    @Autowired
    WorkerData workerData;

    @Value("${smart.ai.auth.server}")
    private String authServerUrl;

    @Bean
    public Map<String, WebClient> webClients(WebClient.Builder builder) {
        Map<String, WebClient> clients = new TreeMap<>();
        for (WorkerData.Worker worker : workerData.getWorkers()) {
            clients.put(worker.getHostname(), builder.baseUrl(worker.getUrl()).build());
        }
        return clients;
    }

    @Bean
    public WebClient authServer(WebClient.Builder builder) {
        return builder.baseUrl(authServerUrl).defaultHeader("Accept", "application/json").build();
    }
}
*/
