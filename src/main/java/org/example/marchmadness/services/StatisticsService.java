package org.example.marchmadness.services;

import org.example.marchmadness.entities.TeamYearStatistics;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Team;
import org.example.marchmadness.repositories.TeamYearStatisticsRepository;
import org.example.marchmadness.repositories.TeamYearStatisticsRepository.TeamYearStatisticsUpdate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class StatisticsService {
    private final TeamYearStatisticsRepository teamYearStatisticsRepository;
    
    
    public StatisticsService(TeamYearStatisticsRepository teamYearStatisticsRepository) {
        this.teamYearStatisticsRepository = teamYearStatisticsRepository;
    }

    /**
     * Command: Retrieve one team's season statistics for a requested year.
     * Preconditions: The team name and year identify a season that exists in the statistics source.
     * Postconditions: Returns the matching season statistics or throws when none exists.
     */
    public TeamYearStatistics getTeamSeasonStatistics(String teamName, int year) {
        return teamYearStatisticsRepository.findByTeamAndYear(teamName, year)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No statistics found for " + teamName + " in " + year
                ));
    }

    /**
     * Command: Retrieve every team's generated statistics for a requested year.
     * Preconditions: The year identifies generated statistics rows in the statistics source.
     * Postconditions: Returns all matching season statistics sorted for leaderboard display.
     */
    public List<TeamYearStatistics> getYearStatistics(int year) {
        return teamYearStatisticsRepository.findByYear(year);
    }

    /**
     * Command: Persist aggregate team statistics from one completed bracket simulation.
     * Preconditions: The bracket is complete, PostgreSQL is enabled, and rows already exist for each team/year.
     * Postconditions: Updates bracket and game counters for every team that played in the bracket.
     */
    public void updateBracketSimulationStatistics(Bracket bracket) {
        Map<TeamYearKey, TeamStatisticsDelta> statisticsByTeam = calculateBracketSimulationStatistics(bracket);

        if (statisticsByTeam.isEmpty()) {
            throw new IllegalArgumentException("Cannot update statistics for a bracket with no games.");
        }

        teamYearStatisticsRepository.applyStatisticsUpdates(toStatisticsUpdates(statisticsByTeam));
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
     * Command: Convert calculated statistics deltas into repository update requests.
     * Preconditions: The service has calculated one delta per team-year.
     * Postconditions: Returns immutable update requests ready for persistence.
     */
    private List<TeamYearStatisticsUpdate> toStatisticsUpdates(
            Map<TeamYearKey, TeamStatisticsDelta> statisticsByTeam
    ) {
        return statisticsByTeam.entrySet()
                .stream()
                .map(entry -> {
                    TeamYearKey key = entry.getKey();
                    TeamStatisticsDelta statistics = entry.getValue();

                    return new TeamYearStatisticsUpdate(
                            key.team(),
                            key.year(),
                            statistics.bracketSimulations(),
                            statistics.bracketWins(),
                            statistics.bracketsPlayed(),
                            statistics.gameSimulations(),
                            statistics.gameWins()
                    );
                })
                .toList();
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
