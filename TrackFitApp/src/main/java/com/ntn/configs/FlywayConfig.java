package com.ntn.configs;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flyway(
            DataSource dataSource,
            @Value("${flyway.baseline-on-migrate:true}") boolean baselineOnMigrate
    ) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion("1")
                .validateMigrationNaming(true)
                .load();
    }

    @Bean
    public Object flywayMigration(
            Flyway flyway,
            @Value("${flyway.enabled:true}") boolean enabled
    ) {
        if (enabled) {
            flyway.migrate();
        }
        return new Object();
    }
}
