package org.example.marchmadness.util;

import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Team;
import java.util.List;
import java.util.Optional;

public final class SimulationUtil {
    private SimulationUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Command: Simulate a game between two teams that may come from different years.
     * Preconditions: Team names are non-blank and years exist in dataset resources.
     * Postconditions: Returns a simulated `Game` when both teams are found, otherwise throws a descriptive error.
     */
    public static Game simulateGame(String name1, int year1, String name2, int year2) {
        validateTeamNameOrThrow(name1, "name1");
        validateTeamNameOrThrow(name2, "name2");

        DatasetUtil.validateYearSupportedOrThrow(year1);
        DatasetUtil.validateYearSupportedOrThrow(year2);

        Team firstTeam = findTeamInYearOrThrow(name1, year1);
        Team secondTeam = findTeamInYearOrThrow(name2, year2);

        return new Game(firstTeam, secondTeam);
    }

    /**
     * Command: Validate a team name input for game simulation.
     * Preconditions: `fieldName` is non-null.
     * Postconditions: Returns normally when valid, otherwise throws an argument error.
     */
    private static void validateTeamNameOrThrow(String teamName, String fieldName) {
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Team name `" + fieldName + "` must be non-empty");
        }
    }

    /**
     * Command: Find a team in all region datasets for a single tournament year.
     * Preconditions: Requested year is supported by `DatasetUtil`.
     * Postconditions: Returns a year-stamped `Team`, or throws if no matching team exists.
     */
    private static Team findTeamInYearOrThrow(String teamName, int year) {
        List<Team> teamsForYear = DatasetUtil.getTeamObjectsForYear(year);
        Optional<Team> matchedTeam = teamsForYear.stream()
                .filter(team -> team.getName() != null)
                .filter(team -> team.getName().trim().equalsIgnoreCase(teamName.trim()))
                .findFirst();

        if (matchedTeam.isEmpty()) {
            throw new IllegalArgumentException(
                    "Team `" + teamName + "` does not exist in year " + year
            );
        }

        Team teamCopy = new Team(matchedTeam.get());
        teamCopy.setYear(year);
        return teamCopy;
    }
}
