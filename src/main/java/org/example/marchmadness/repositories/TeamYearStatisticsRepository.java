package org.example.marchmadness.repositories;

import org.example.marchmadness.entities.TeamYearStatistics;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class TeamYearStatisticsRepository {

    private static final String FIND_BY_TEAM_AND_YEAR_SQL = """
            SELECT id, team, year, seed, rating, bracketsimulations, bracketwins, gamesimulations, gamewins
            FROM teamyearstatistics
            WHERE team = ? AND year = ?
            """;

    private final ObjectProvider<DataSource> dataSourceProvider;

    public TeamYearStatisticsRepository(ObjectProvider<DataSource> dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    /*
     * Command: Find one team's statistics for one tournament year.
     * Pre-condition: PostgreSQL is enabled and the teamyearstatistics table exists.
     * Post-condition: Returns the matching statistics row when it exists, otherwise returns Optional.empty().
     *
     * Looks up a TeamYearStatistics row using the DataSource created by AwsPsqlDatabaseConfiguration.
     */
    public Optional<TeamYearStatistics> findByTeamAndYear(String teamName, int year) {
        try (
                Connection connection = dataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_TEAM_AND_YEAR_SQL)
        ) {
            statement.setString(1, teamName);
            statement.setInt(2, year);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(toTeamYearStatistics(resultSet));
            }
        } catch (SQLException exc) {
            throw new IllegalStateException("Failed to find team year statistics.", exc);
        }
    }

    /*
     * Command: Get the configured application DataSource.
     * Pre-condition: AWS PostgreSQL has been enabled in application configuration.
     * Post-condition: Returns the configured DataSource or throws a clear exception when none exists.
     *
     * Keeps missing database configuration errors consistent for repository methods.
     */
    private DataSource dataSource() {
        DataSource dataSource = dataSourceProvider.getIfAvailable();

        if (dataSource == null) {
            throw new IllegalStateException("PostgreSQL is not configured. Set AWS_PSQL_ENABLED=true and database settings.");
        }

        return dataSource;
    }

    /*
     * Command: Convert the current SQL result row into a statistics entity.
     * Pre-condition: The ResultSet cursor is positioned on a teamyearstatistics row.
     * Post-condition: Returns a TeamYearStatistics object populated from that row.
     *
     * Centralizes database-to-entity mapping for this repository.
     */
    private TeamYearStatistics toTeamYearStatistics(ResultSet resultSet) throws SQLException {
        TeamYearStatistics statistics = new TeamYearStatistics(
                resultSet.getString("team"),
                resultSet.getInt("year"),
                resultSet.getInt("seed")
        );

        statistics.setId(resultSet.getLong("id"));
        statistics.setRating(resultSet.getDouble("rating"));
        statistics.setBracketSimulations(resultSet.getInt("bracketsimulations"));
        statistics.setBracketWins(resultSet.getInt("bracketwins"));
        statistics.setGameSimulations(resultSet.getInt("gamesimulations"));
        statistics.setGameWins(resultSet.getInt("gamewins"));

        return statistics;
    }
}
