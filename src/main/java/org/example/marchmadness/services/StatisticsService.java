package org.example.marchmadness.services;

import org.example.marchmadness.models.TeamYearStatistics;
import org.example.marchmadness.configuration.AwsPsqlDatabaseConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;


@Service
public class StatisticsService {
    private final ObjectProvider<AwsPsqlDatabaseConfiguration> postgresConnectionProvider;
    
    
    public StatisticsService(ObjectProvider<AwsPsqlDatabaseConfiguration> postgresConnectionProvider) {
        this.postgresConnectionProvider = postgresConnectionProvider;
    }

    /**
     * Command: Retrieve one team's season statistics for a requested year.
     * Preconditions: The team name and year identify a season that exists in the statistics source.
     * Postconditions: Returns the matching season statistics, or null until database lookup is implemented.
     */
    public TeamYearStatistics getTeamSeasonStatistics(String teamName, int year) {
        return null;
    }
}
