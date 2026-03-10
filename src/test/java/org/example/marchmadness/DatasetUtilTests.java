package org.example.marchmadness;

import org.example.marchmadness.models.Team;
import org.example.marchmadness.util.DatasetUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasetUtilTests {

    @Test
    @DisplayName("Dataset metadata returns sorted available years")
    void datasetMetadata_returnsSortedAvailableYears() {
        List<Integer> years = DatasetUtil.getAvailableYears();
        assertTrue(years.contains(2023));
        assertTrue(years.contains(2024));
        assertTrue(years.indexOf(2023) > years.indexOf(2024));
    }

    @Test
    @DisplayName("Dataset utility returns all teams for a valid year")
    void datasetUtility_returnsAllTeamsForYear() {
        List<Team> teams = DatasetUtil.getTeamsForYear(2024);
        assertFalse(teams.isEmpty());
        assertTrue(teams.stream().anyMatch(team -> "Connecticut".equals(team.getName())));
    }

    @Test
    @DisplayName("Dataset utility throws when year is invalid")
    void datasetUtility_throwsForInvalidYear() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DatasetUtil.getTeamsForYear(1900)
        );
        assertTrue(exception.getMessage().contains("Year 1900 is not available"));
    }
}
