package org.example.marchmadness.metadata;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class DatasetMetadata {
    private static final String DATASET_YEAR_PATTERN = "classpath*:datasets/*";

    private DatasetMetadata() {
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
}