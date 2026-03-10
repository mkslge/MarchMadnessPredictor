package org.example.marchmadness.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Team;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SimulationUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String YEAR_DATASET_PATTERN = "classpath*:datasets/%d/*.json";

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
        List<Team> teamsForYear = loadTeamsForYear(year);
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

    /**
     * Command: Load all teams from every region dataset for a specific year.
     * Preconditions: Dataset files for the year are present and valid JSON.
     * Postconditions: Returns a combined list of teams across all regions for that year.
     */
    private static List<Team> loadTeamsForYear(int year) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Team> yearTeams = new ArrayList<>();

        try {
            Resource[] resources = resolver.getResources(YEAR_DATASET_PATTERN.formatted(year));
            if (resources.length == 0) {
                throw new IllegalArgumentException("No dataset files found for year " + year);
            }

            for (Resource resource : resources) {
                yearTeams.addAll(loadTeamsFromRegionResource(resource));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read team datasets for year " + year, exception);
        }

        return yearTeams;
    }

    /**
     * Command: Parse one regional dataset file into team models.
     * Preconditions: Resource stream is readable and contains a `Region` team array.
     * Postconditions: Returns teams from the provided region resource.
     */
    private static List<Team> loadTeamsFromRegionResource(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode rootNode = OBJECT_MAPPER.readTree(inputStream);
            JsonNode regionNode = rootNode.get("Region");
            if (regionNode == null || !regionNode.isArray()) {
                throw new IllegalArgumentException(
                        "Dataset file `" + resource.getFilename() + "` is missing a valid `Region` array"
                );
            }
            return OBJECT_MAPPER.readValue(regionNode.toString(), new TypeReference<>() {});
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to parse dataset file `" + resource.getFilename() + "`",
                    exception
            );
        }
    }
}
