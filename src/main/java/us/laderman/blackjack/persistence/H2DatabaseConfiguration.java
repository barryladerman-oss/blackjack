package us.laderman.blackjack.persistence;

import java.util.Objects;

import javax.sql.DataSource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "blackjack.database.mode", havingValue = "h2")
@MapperScan("us.laderman.blackjack.persistence.mapper")
public class H2DatabaseConfiguration {
    @Bean
    DataSource h2DataSource() {
        DataSource dataSource = Objects.requireNonNull(DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:blackjack;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .username("sa")
                .password("")
                .build());

        ResourceDatabasePopulator schema = new ResourceDatabasePopulator(
                new ClassPathResource("db/schema-h2.sql"));
        DatabasePopulatorUtils.execute(schema, dataSource);
        return dataSource;
    }
}
