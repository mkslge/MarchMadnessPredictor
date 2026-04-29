package org.example.marchmadness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.example.marchmadness.configuration.AwsPsqlDatabaseConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class AwsPsqlDatabaseConnectionTests {

    private static final String AWS_PSQL_HOST = "AWS_PSQL_HOST";
    private static final String AWS_PSQL_PORT = "AWS_PSQL_PORT";
    private static final String AWS_PSQL_DATABASE = "AWS_PSQL_DATABASE";
    private static final String AWS_PSQL_USERNAME = "AWS_PSQL_USERNAME";
    private static final String AWS_PSQL_PASSWORD = "AWS_PSQL_PASSWORD";

    @Test
    @DisplayName("AWS PostgreSQL database accepts a connection and responds to a health query")
    void awsPsqlDatabaseConnection_returnsHealthQueryResult() throws Exception {
        assumeTrue(requiredEnvironmentVariablesArePresent(),
                "Skipping AWS PostgreSQL connection test because database environment variables are missing.");

        DataSource dataSource = createAwsPsqlDataSourceFromEnvironment();

        try {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                assertTrue(connection.isValid(5));
                assertTrue(resultSet.next());
                assertEquals(1, resultSet.getInt(1));
            }
        } finally {
            if (dataSource instanceof AutoCloseable closeableDataSource) {
                closeableDataSource.close();
            }
        }
    }

    /*
     * Pre-condition: The AWS PostgreSQL environment variables may or may not be
     * present on the machine running the test.
     * Post-condition: Returns true only when all required connection values are
     * available.
     *
     * Checks whether this machine has enough configuration to run the live
     * database connection test.
     */
    private boolean requiredEnvironmentVariablesArePresent() {
        return !environmentVariable(AWS_PSQL_HOST).isBlank()
                && !environmentVariable(AWS_PSQL_DATABASE).isBlank()
                && !environmentVariable(AWS_PSQL_USERNAME).isBlank()
                && !environmentVariable(AWS_PSQL_PASSWORD).isBlank();
    }

    /*
     * Pre-condition: Required AWS PostgreSQL environment variables are present.
     * Post-condition: Returns a DataSource configured with the same properties
     * the application uses for AWS PostgreSQL.
     *
     * Creates the database DataSource through the application configuration class
     * so the test verifies the real configuration path.
     */
    private DataSource createAwsPsqlDataSourceFromEnvironment() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("aws.psql.enabled", "true")
                .withProperty("aws.psql.host", environmentVariable(AWS_PSQL_HOST))
                .withProperty("aws.psql.port", environmentVariableOrDefault(AWS_PSQL_PORT, "5432"))
                .withProperty("aws.psql.database", environmentVariable(AWS_PSQL_DATABASE))
                .withProperty("aws.psql.username", environmentVariable(AWS_PSQL_USERNAME))
                .withProperty("aws.psql.password", environmentVariable(AWS_PSQL_PASSWORD));

        return new AwsPsqlDatabaseConfiguration().awsPsqlDataSource(environment);
    }

    /*
     * Pre-condition: The requested environment variable may or may not exist.
     * Post-condition: Returns the environment variable value or an empty string.
     *
     * Normalizes missing environment variables so blank checks stay simple.
     */
    private String environmentVariable(String name) {
        return System.getenv().getOrDefault(name, "");
    }

    /*
     * Pre-condition: The requested environment variable may or may not exist.
     * Post-condition: Returns the environment variable value or the provided
     * default value.
     *
     * Supplies the default PostgreSQL port when a custom AWS_PSQL_PORT is not set.
     */
    private String environmentVariableOrDefault(String name, String defaultValue) {
        return System.getenv().getOrDefault(name, defaultValue);
    }
}
