package org.example.marchmadness.services;

import org.example.marchmadness.entities.TeamYearStatistics;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Team;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;


@Service
public class StatisticsService {
    private static final String UPDATE_TEAM_YEAR_STATISTICS_SQL = """
            UPDATE teamyearstatistics
            SET bracketsimulations = bracketsimulations + ?,
                bracketwins = bracketwins + ?,
                bracketsplayed = bracketsplayed + ?,
                gamesimulations = gamesimulations + ?,
                gamewins = gamewins + ?
            WHERE team = ? AND year = ?
            """;

    private final ObjectProvider<DataSource> dataSourceProvider;
    
    
    public StatisticsService(ObjectProvider<DataSource> dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    /**
     * Command: Retrieve one team's season statistics for a requested year.
     * Preconditions: The team name and year identify a season that exists in the statistics source.
     * Postconditions: Returns the matching season statistics, or null until database lookup is implemented.
     */
    public TeamYearStatistics getTeamSeasonStatistics(String teamName, int year) {
        return null;
    }

    /**
     * Command: Persist aggregate team statistics from one completed bracket simulation.
     * Preconditions: The bracket is complete, PostgreSQL is enabled, and rows already exist for each team/year.
     * Postconditions: Updates bracket and game counters for every team that played in the bracket.
     */
    public void updateBracketSimulationStatistics(Bracket bracket) {
        Map<TeamYearKey, TeamStatisticsDelta> statisticsByTeam = calculateBracketSimulationStatistics(bracket);
        DataSource dataSource = dataSourceProvider.getIfAvailable();

        if (statisticsByTeam.isEmpty()) {
            throw new IllegalArgumentException("Cannot update statistics for a bracket with no games.");
        }

        if (dataSource == null) {
            throw new IllegalStateException("PostgreSQL is not configured. Set AWS_PSQL_ENABLED=true and database settings.");
        }

        updateTeamYearStatistics(dataSource, statisticsByTeam);
    }

    /**
     * Command: Calculate per-team counter changes for one completed bracket simulation.
     * Preconditions: The bracket has been simulated and contains all games.
     * Postconditions: Returns one statistics delta per team that appeared in the bracket.
     */
    private Map<TeamYearKey, TeamStatisticsDelta> calculateBracketSimulationStatistics(Bracket bracket) {
        Map<TeamYearKey, TeamStatisticsDelta> statisticsByTeam = new LinkedHashMap<>();

        for (Game game : bracket.collectGames()) {
            Team winner = game.getWinner();
            Team loser = game.getLoser();

            TeamStatisticsDelta winnerStatistics = statisticsByTeam.computeIfAbsent(
                    keyFor(winner, bracket.year()),
                    ignoredKey -> new TeamStatisticsDelta()
            );
            winnerStatistics.addGameSimulation();
            winnerStatistics.addGameWin();

            TeamStatisticsDelta loserStatistics = statisticsByTeam.computeIfAbsent(
                    keyFor(loser, bracket.year()),
                    ignoredKey -> new TeamStatisticsDelta()
            );
            loserStatistics.addGameSimulation();
        }

        for (TeamStatisticsDelta statistics : statisticsByTeam.values()) {
            statistics.addBracketSimulation();
            statistics.addBracketPlayed();
        }

        Team champion = bracket.getChampion();
        statisticsByTeam.get(keyFor(champion, bracket.year())).addBracketWin();

        return statisticsByTeam;
    }

    /**
     * Command: Build the database key for a simulated team.
     * Preconditions: The team has a name and the bracket year is known.
     * Postconditions: Returns a normalized key for the team's season statistics row.
     */
    private TeamYearKey keyFor(Team team, int bracketYear) {
        int teamYear = team.getYear() == 0 ? bracketYear : team.getYear();
        return new TeamYearKey(team.getName(), teamYear);
    }

    /**
     * Command: Apply calculated statistics changes to PostgreSQL.
     * Preconditions: The DataSource can connect to the database and matching rows exist.
     * Postconditions: Commits all team counter updates, or rolls back if any update fails.
     */
    private void updateTeamYearStatistics(
            DataSource dataSource,
            Map<TeamYearKey, TeamStatisticsDelta> statisticsByTeam
    ) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(UPDATE_TEAM_YEAR_STATISTICS_SQL)) {
                for (Map.Entry<TeamYearKey, TeamStatisticsDelta> entry : statisticsByTeam.entrySet()) {
                    TeamYearKey key = entry.getKey();
                    TeamStatisticsDelta statistics = entry.getValue();

                    statement.setInt(1, statistics.bracketSimulations());
                    statement.setInt(2, statistics.bracketWins());
                    statement.setInt(3, statistics.bracketsPlayed());
                    statement.setInt(4, statistics.gameSimulations());
                    statement.setInt(5, statistics.gameWins());
                    statement.setString(6, key.team());
                    statement.setInt(7, key.year());
                    statement.addBatch();
                }

                int[] updateCounts = statement.executeBatch();
                assertAllTeamsWereUpdated(updateCounts, statisticsByTeam);
                connection.commit();
            } catch (SQLException | RuntimeException exc) {
                rollback(connection);
                throw exc;
            }
        } catch (SQLException exc) {
            throw new IllegalStateException("Failed to update bracket simulation statistics.", exc);
        }
    }

    /**
     * Command: Verify that each calculated team row was updated.
     * Preconditions: A database batch update has completed.
     * Postconditions: Throws an exception if any team/year row was missing.
     */
    private void assertAllTeamsWereUpdated(
            int[] updateCounts,
            Map<TeamYearKey, TeamStatisticsDelta> statisticsByTeam
    ) {
        int index = 0;
        for (TeamYearKey key : statisticsByTeam.keySet()) {
            if (updateCounts[index] == 0 || updateCounts[index] == Statement.EXECUTE_FAILED) {
                throw new IllegalStateException(
                        "No TeamYearStatistics row exists for " + key.team() + " in " + key.year()
                );
            }
            index++;
        }
    }

    /**
     * Command: Roll back a failed database transaction.
     * Preconditions: A connection has an active transaction.
     * Postconditions: Rolls back the transaction or throws if rollback fails.
     */
    private void rollback(Connection connection) throws SQLException {
        connection.rollback();
    }

    private record TeamYearKey(String team, int year) {
    }

    private static class TeamStatisticsDelta {
        private int bracketSimulations;
        private int bracketWins;
        private int bracketsPlayed;
        private int gameSimulations;
        private int gameWins;

        void addBracketSimulation() {
            bracketSimulations++;
        }

        void addBracketWin() {
            bracketWins++;
        }

        void addBracketPlayed() {
            bracketsPlayed++;
        }

        void addGameSimulation() {
            gameSimulations++;
        }

        void addGameWin() {
            gameWins++;
        }

        int bracketSimulations() {
            return bracketSimulations;
        }

        int bracketWins() {
            return bracketWins;
        }

        int bracketsPlayed() {
            return bracketsPlayed;
        }

        int gameSimulations() {
            return gameSimulations;
        }

        int gameWins() {
            return gameWins;
        }
    }
}
