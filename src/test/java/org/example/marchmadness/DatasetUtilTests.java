package org.example.marchmadness;

import org.example.marchmadness.util.DatasetUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
}
