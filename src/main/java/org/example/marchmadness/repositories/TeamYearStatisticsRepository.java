package org.example.marchmadness.repositories;

import org.example.marchmadness.entities.TeamYearStatistics;
import org.example.marchmadness.models.Game;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionOperations;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class TeamYearStatisticsRepository {
    private static final RowMapper<TeamYearStatistics> TEAM_YEAR_STATISTICS_ROW_MAPPER =
            TeamYearStatisticsRepository::toTeamYearStatistics;

    private static final String FIND_BY_TEAM_AND_YEAR_SQL = """
            SELECT id, team, year, seed, rating, bracketsimulations, bracketwins, gamesimulations, gamewins
            FROM teamyearstatistics
            WHERE team = ? AND year = ?
            """;

    private static final String FIND_BY_YEAR_SQL = """
            SELECT id, team, year, seed, rating, bracketsimulations, bracketwins, gamesimulations, gamewins
            FROM teamyearstatistics
            WHERE year = ?
            ORDER BY bracketwins DESC, gamewins DESC, team ASC
            """;

    private static final String UPDATE_TEAM_YEAR_STATISTICS_SQL = """
            UPDATE teamyearstatistics
            SET bracketsimulations = bracketsimulations + ?,
                bracketwins = bracketwins + ?,
                bracketsplayed = bracketsplayed + ?,
                gamesimulations = gamesimulations + ?,
                gamewins = gamewins + ?
            WHERE team = ? AND year = ?
            """;

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<TransactionOperations> transactionOperationsProvider;

    public TeamYearStatisticsRepository(
            ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            ObjectProvider<TransactionOperations> transactionOperationsProvider
    ) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.transactionOperationsProvider = transactionOperationsProvider;
    }

    /*
     * Command: Find one team's statistics for one tournament year.
     * Pre-condition: PostgreSQL is enabled and the teamyearstatistics table exists.
     * Post-condition: Returns the matching statistics row when it exists, otherwise returns Optional.empty().
     *
     * Looks up a TeamYearStatistics row using the DataSource created by AwsPsqlDatabaseConfiguration.
     */
    public Optional<TeamYearStatistics> findByTeamAndYear(String teamName, int year) {
        List<TeamYearStatistics> statistics = jdbcTemplate().query(
                FIND_BY_TEAM_AND_YEAR_SQL,
                TEAM_YEAR_STATISTICS_ROW_MAPPER,
                teamName,
                year
        );

        return statistics.stream().findFirst();
    }

    /*
     * Command: Find every team's statistics for one tournament year.
     * Pre-condition: PostgreSQL is enabled and the teamyearstatistics table exists.
     * Post-condition: Returns all matching statistics rows sorted by bracket wins, game wins, then team name.
     *
     * Supports year-level statistics screens without requiring callers to query every team individually.
     */
    public List<TeamYearStatistics> findByYear(int year) {
        return jdbcTemplate().query(
                FIND_BY_YEAR_SQL,
                TEAM_YEAR_STATISTICS_ROW_MAPPER,
                year
        );
    }

    /*
     * Command: Apply counter increments for multiple team-year statistics rows.
     * Pre-condition: PostgreSQL is enabled and every update targets an existing teamyearstatistics row.
     * Post-condition: Commits all counter updates, or rolls back if any update fails or targets no row.
     *
     * Keeps batch SQL and transaction handling inside the repository persistence boundary.
     */
    public void applyStatisticsUpdates(Collection<TeamYearStatisticsUpdate> statisticsUpdates) {
        if (statisticsUpdates.isEmpty()) {
            throw new IllegalArgumentException("Cannot update statistics without any team updates.");
        }

        transactionOperations().executeWithoutResult(ignoredStatus -> {
            int[] updateCounts = jdbcTemplate().batchUpdate(
                    UPDATE_TEAM_YEAR_STATISTICS_SQL,
                    new TeamYearStatisticsUpdateBatch(statisticsUpdates)
            );

            assertAllTeamsWereUpdated(updateCounts, statisticsUpdates);
        });
    }

    

    /*
     * Command: Get the configured application JdbcTemplate.
     * Pre-condition: AWS PostgreSQL has been enabled in application configuration.
     * Post-condition: Returns the configured JdbcTemplate or throws a clear exception when none exists.
     *
     * Keeps missing database configuration errors consistent for repository methods.
     */
    private JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();

        if (jdbcTemplate == null) {
            throw new IllegalStateException("PostgreSQL is not configured. Set AWS_PSQL_ENABLED=true and database settings.");
        }

        return jdbcTemplate;
    }

    /*
     * Command: Get the configured transaction helper.
     * Pre-condition: AWS PostgreSQL has been enabled in application configuration.
     * Post-condition: Returns the configured transaction helper or throws a clear exception when none exists.
     *
     * Keeps write methods atomic without manually managing commit and rollback.
     */
    private TransactionOperations transactionOperations() {
        TransactionOperations transactionOperations = transactionOperationsProvider.getIfAvailable();

        if (transactionOperations == null) {
            throw new IllegalStateException("PostgreSQL transactions are not configured. Set AWS_PSQL_ENABLED=true and database settings.");
        }

        return transactionOperations;
    }

    /*
     * Command: Verify that each requested team statistics row was updated.
     * Pre-condition: A database batch update has completed.
     * Post-condition: Throws an exception if any team/year row was missing.
     *
     * Protects callers from silently dropping statistics for unknown teams.
     */
    private void assertAllTeamsWereUpdated(
            int[] updateCounts,
            Collection<TeamYearStatisticsUpdate> statisticsUpdates
    ) {
        int index = 0;
        for (TeamYearStatisticsUpdate statisticsUpdate : statisticsUpdates) {
            if (updateCounts[index] == 0 || updateCounts[index] == Statement.EXECUTE_FAILED) {
                throw new IllegalStateException(
                        "No TeamYearStatistics row exists for "
                                + statisticsUpdate.team()
                                + " in "
                                + statisticsUpdate.year()
                );
            }
            index++;
        }
    }

    /*
     * Command: Convert the current SQL result row into a statistics entity.
     * Pre-condition: The ResultSet cursor is positioned on a teamyearstatistics row.
     * Post-condition: Returns a TeamYearStatistics object populated from that row.
     *
     * Centralizes database-to-entity mapping for this repository.
     */
    private static TeamYearStatistics toTeamYearStatistics(ResultSet resultSet, int rowNumber) throws SQLException {
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

    private static class TeamYearStatisticsUpdateBatch implements BatchPreparedStatementSetter {
        private final List<TeamYearStatisticsUpdate> statisticsUpdates;

        TeamYearStatisticsUpdateBatch(Collection<TeamYearStatisticsUpdate> statisticsUpdates) {
            this.statisticsUpdates = List.copyOf(statisticsUpdates);
        }

        /*
         * Command: Bind one team-year statistics update into a batch statement.
         * Pre-condition: The batch index points to an existing update request.
         * Post-condition: The PreparedStatement contains all values needed for that update.
         *
         * Keeps SQL parameter order close to the update record fields.
         */
        @Override
        public void setValues(PreparedStatement statement, int index) throws SQLException {
            TeamYearStatisticsUpdate statisticsUpdate = statisticsUpdates.get(index);

            statement.setInt(1, statisticsUpdate.bracketSimulations());
            statement.setInt(2, statisticsUpdate.bracketWins());
            statement.setInt(3, statisticsUpdate.bracketsPlayed());
            statement.setInt(4, statisticsUpdate.gameSimulations());
            statement.setInt(5, statisticsUpdate.gameWins());
            statement.setString(6, statisticsUpdate.team());
            statement.setInt(7, statisticsUpdate.year());
        }

        @Override
        public int getBatchSize() {
            return statisticsUpdates.size();
        }
    }

    public record TeamYearStatisticsUpdate(
            String team,
            int year,
            int bracketSimulations,
            int bracketWins,
            int bracketsPlayed,
            int gameSimulations,
            int gameWins
    ) {
    }
}
