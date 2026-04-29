package org.example.marchmadness.configuration;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnProperty(prefix = "aws.psql", name = "enabled", havingValue = "true")
public class AwsPsqlDatabaseConfiguration {

    private static final String POSTGRES_DRIVER_CLASS_NAME = "org.postgresql.Driver";

    /*
     * Pre-condition: aws.psql.enabled is true and AWS PostgreSQL connection
     * properties are available from application.properties or environment variables.
     * Post-condition: Spring has a DataSource bean connected to the configured
     * PostgreSQL database.
     *
     * Builds the DataSource used by the application when AWS PostgreSQL is enabled.
     */
    @Bean
    public DataSource awsPsqlDataSource(Environment environment) {
        String host = environment.getProperty("aws.psql.host", "");
        String port = environment.getProperty("aws.psql.port", "5432");
        String databaseName = environment.getProperty("aws.psql.database", "");
        String username = environment.getProperty("aws.psql.username", "");
        String password = environment.getProperty("aws.psql.password", "");

        if (host.isBlank() || databaseName.isBlank() || username.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "AWS PostgreSQL is enabled, but host, database, username, or password is missing.");
        }

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;

        return DataSourceBuilder.create()
                .driverClassName(POSTGRES_DRIVER_CLASS_NAME)
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .build();
    }
}
