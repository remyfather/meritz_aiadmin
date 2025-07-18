package smart.ai.admin.v2.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "smart.ai.admin.v2.repository")
@EnableTransactionManagement
@ConditionalOnProperty(name = "smart.ai.database.mode", havingValue = "mysql")
public class DatabaseConfigV2 {
    // MySQL 설정이 활성화된 경우에만 로드됨
} 