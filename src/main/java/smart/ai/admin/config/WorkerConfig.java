package smart.ai.admin.config;

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
