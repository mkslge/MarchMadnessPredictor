package org.example.marchmadness.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.marchmadness.models.Team;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class DatasetUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DATASET_YEAR_PATTERN = "classpath*:datasets/*";
    private static final String YEAR_DATASET_PATTERN = "classpath*:datasets/%d/*.json";

    private DatasetUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Command: Discover all available dataset years from classpath resources.
     * Preconditions: Dataset folders exist under `resources/datasets/{year}`.
     * Postconditions: Returns a sorted list of unique available years.
     */
    public static List<Integer> getAvailableYears() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Set<Integer> availableYears = new TreeSet<>(Comparator.reverseOrder());
        try {
            Resource[] resources = resolver.getResources(DATASET_YEAR_PATTERN);
            for (Resource resource : resources) {
                String folderName = resource.getFilename();
                if (folderName == null || !folderName.matches("\\d{4}")) {
                    continue;
                }
                availableYears.add(Integer.parseInt(folderName));
            }
            return List.copyOf(availableYears);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to discover available dataset years", exception);
        }
    }

    /**
     * Command: Build a year-not-supported error that includes all available years.
     * Preconditions: Dataset discovery can run successfully.
     * Postconditions: Returns an `IllegalArgumentException` with supported-year context.
     */
    public static IllegalArgumentException buildUnavailableYearError(int requestedYear) {
        List<Integer> availableYears = getAvailableYears();
        return new IllegalArgumentException(
                "Year " + requestedYear + " is not available. Available years: " + availableYears
        );
    }

    /**
     * Command: Validate a requested year against discovered dataset years.
     * Preconditions: Dataset discovery can run successfully.
     * Postconditions: Returns normally when supported, otherwise throws unsupported-year error.
     */
    public static void validateYearSupportedOrThrow(int requestedYear) {
        if (!getAvailableYears().contains(requestedYear)) {
            throw buildUnavailableYearError(requestedYear);
        }
    }

    /**
     * Command: Load all tournament team names for a specific year from regional datasets.
     * Preconditions: `year` is available and dataset files are valid JSON with a `Region` array.
     * Postconditions: Returns all team names found for that year across all region files.
     */
    public static String[] getTeamsForYear(int year) {
        List<Team> teams = getTeamObjectsForYear(year);
        return teams.stream()
                .map(Team::getName)
                .toArray(String[]::new);
    }

    /**
     * Command: Load all tournament team models for a specific year from regional datasets.
     * Preconditions: `year` is available and dataset files are valid JSON with a `Region` array.
     * Postconditions: Returns all teams found for that year across all region files.
     */
    static List<Team> getTeamObjectsForYear(int year) {
        validateYearSupportedOrThrow(year);

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
            return List.copyOf(yearTeams);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read team datasets for year " + year, exception);
        }
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
