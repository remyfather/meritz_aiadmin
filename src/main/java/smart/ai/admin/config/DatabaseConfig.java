package smart.ai.admin.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;


@Configuration
public class DatabaseConfig {
    
    @Value("${mssql.jdbc.url}")
    private String mssqlUrl;

    @Value("${mssql.jdbc.username}")
    private String mssqlUsername;

    @Value("${mssql.jdbc.password}")
    private String mssqlPassword;

    @Value("${mssql.jdbc.driver-class-name}")
    private String mssqlDriverClassName;

    @Value("${mssql.jdbc.maximum-pool-size:10}")
    private int mssqlMaxPoolSize;

    @Value("${db2.jdbc.url}")
    private String db2Url;

    @Value("${db2.jdbc.username}")
    private String db2Username;

    @Value("${db2.jdbc.password}")
    private String db2Password;

    @Value("${db2.jdbc.driver-class-name}")
    private String db2DriverClassName;

    @Value("${db2.jdbc.maximum-pool-size:10}")
    private int db2MaxPoolSize;

    @Value("${oracle.jdbc.url}")
    private String oracleUrl;

    @Value("${oracle.jdbc.username}")
    private String oracleUsername;

    @Value("${oracle.jdbc.password}")
    private String oraclePassword;

    @Value("${oracle.jdbc.driver-class-name}")
    private String oracleDriverClassName;

    @Value("${oracle.jdbc.maximum-pool-size:10}")
    private int oracleMaxPoolSize;
    


    // MSSQL DataSource (기존)
    @Bean(name = "mssqlDataSource")
    @Primary
    public DataSource mssqlDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(mssqlDriverClassName);
        dataSource.setUrl(mssqlUrl);
        dataSource.setUsername(mssqlUsername);
        dataSource.setPassword(mssqlPassword);

        dataSource.setInitialSize(5);
        dataSource.setMaxTotal(mssqlMaxPoolSize);
        dataSource.setMinIdle(5);
        dataSource.setMaxIdle(10);
        dataSource.setMaxWaitMillis(10000);
        // return DataSourceBuilder.create()
        //                         .url(mssqlUrl)
        //                         .username(mssqlUsername)
        //                         .password(mssqlPassword)
        //                         .driverClassName(mssqlDriverClassName)
        //                         .build();

        return dataSource;
    }
    // 오라클 DataSource 추가
    @Bean(name = "oracleDataSource")
    public DataSource oracleDataSource() {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(oracleDriverClassName);
        dataSource.setUrl(oracleUrl);
        dataSource.setUsername(oracleUsername);
        dataSource.setPassword(oraclePassword);

        dataSource.setInitialSize(5);
        dataSource.setMaxTotal(oracleMaxPoolSize);
        dataSource.setMinIdle(5);
        dataSource.setMaxIdle(10);
        dataSource.setMaxWaitMillis(10000);
                // return DataSourceBuilder.create()
                //                         .url(oracleUrl)
                //                         .username(oracleUsername)
                //                         .password(oraclePassword)
                //                         .driverClassName(oracleDriverClassName)
                //                         .build();
        return dataSource;
    }

    // db2 Datasource 추가
    @Bean(name = "db2DataSource")
    public DataSource db2DataSource() {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(db2DriverClassName);
        dataSource.setUrl(db2Url);
        dataSource.setUsername(db2Username);
        dataSource.setPassword(db2Password);

        dataSource.setInitialSize(5);
        dataSource.setMaxTotal(db2MaxPoolSize);
        dataSource.setMinIdle(5);
        dataSource.setMaxIdle(10);
        dataSource.setMaxWaitMillis(10000);

        // return DataSourceBuilder.create()
        //                         .url(db2Url)
        //                         .username(db2Username)
        //                         .password(db2Password)
        //                         .driverClassName(db2DriverClassName)
        //                         .build();
        return dataSource;
    }

    @Bean(name = "oracleJdbcTemplate")
    public JdbcTemplate oracleJdbcTemplate(@Qualifier("oracleDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "mssqlJdbcTemplate")
    public JdbcTemplate mssqlJdbcTemplate(@Qualifier("mssqlDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "db2JdbcTemplate")
    public JdbcTemplate db2JdbcTemplate(@Qualifier("db2DataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
