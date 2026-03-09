package org.example.marchmadness.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Bracket {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonProperty
    private final int year;

    @JsonProperty
    private Team champion;

    @JsonProperty
    private FinalFour finalFour;

    @JsonProperty
    private final List<Region> regions = new ArrayList<>();

    public Bracket(int year) {
        this.year = year;
    }

    /**
     * Command: Run a full tournament simulation for this bracket year.
     * Preconditions: Year is valid and all region datasets exist.
     * Postconditions: Regions, Final Four, and champion are populated.
     */
    public void run() {
        initRegions();
        finalFour = new FinalFour(regions.get(0), regions.get(1), regions.get(2), regions.get(3));
        champion = finalFour.getChampion();
    }

    /**
     * Command: Initialize regional simulations in fixed tournament order.
     * Preconditions: Bracket year is set.
     * Postconditions: `regions` contains EAST, MIDWEST, SOUTH, WEST simulations.
     */
    private void initRegions() {
        regions.clear();
        regions.add(new Region(RegionType.EAST, year));
        regions.add(new Region(RegionType.MIDWEST, year));
        regions.add(new Region(RegionType.SOUTH, year));
        regions.add(new Region(RegionType.WEST, year));
    }

    public String getChampionName() {
        return finalFour.getChampion().getName();
    }

    public Team getChampion() {
        return finalFour.getChampion();
    }

    /**
     * Command: Serialize this bracket into a JSON string.
     * Preconditions: Bracket state is initialized.
     * Postconditions: Returns a JSON representation of the current bracket.
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (IOException exc) {
            throw new IllegalStateException("Failed to serialize Bracket to JSON", exc);
        }
    }
}
