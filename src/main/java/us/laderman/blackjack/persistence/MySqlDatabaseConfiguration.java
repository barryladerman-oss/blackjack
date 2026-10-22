package us.laderman.blackjack.persistence;

import javax.sql.DataSource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "blackjack.database.mode", havingValue = "mysql", matchIfMissing = true)
@MapperScan("us.laderman.blackjack.persistence.mapper")
public class MySqlDatabaseConfiguration {
    @Bean
    DataSource mysqlDataSource(
            @Value("${blackjack.database.mysql.url:jdbc:mysql://localhost:3306/blackjack}") String url,
            @Value("${blackjack.database.mysql.username:root}") String username,
            @Value("${blackjack.database.mysql.password:}") String password) {
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}
