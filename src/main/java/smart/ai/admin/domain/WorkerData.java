package smart.ai.admin.domain;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix="smart.ai")
public class WorkerData {

    private List<Worker> workers;

    @Data
    public static class Worker {
        private String hostname;
        private String url;
    }

}
