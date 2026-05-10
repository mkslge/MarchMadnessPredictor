package org.example.marchmadness.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Region {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonProperty
    private final RegionType region;

    @JsonProperty
    private final int year;

    @JsonProperty
    private final List<Game> fieldOf64;

    @JsonProperty
    private final List<Game> fieldOf32;

    @JsonProperty
    private final List<Game> sweet16;

    @JsonProperty
    private final List<Game> elite8;

    @JsonProperty
    private final Team regionWinner;

    /**
     * Command: Create a completed regional tournament result.
     * Preconditions: Region type, round game lists, and winner are non-null.
     * Postconditions: Region stores immutable copies of all round results and the regional winner.
     */
    public Region(
            RegionType region,
            int year,
            List<Game> fieldOf64,
            List<Game> fieldOf32,
            List<Game> sweet16,
            List<Game> elite8,
            Team regionWinner
    ) {
        if (region == null
                || fieldOf64 == null
                || fieldOf32 == null
                || sweet16 == null
                || elite8 == null
                || regionWinner == null) {
            throw new IllegalArgumentException("Region result values cannot be null");
        }
        this.region = region;
        this.year = year;
        this.fieldOf64 = List.copyOf(fieldOf64);
        this.fieldOf32 = List.copyOf(fieldOf32);
        this.sweet16 = List.copyOf(sweet16);
        this.elite8 = List.copyOf(elite8);
        this.regionWinner = new Team(regionWinner);
    }

    public RegionType getRegion() {
        return region;
    }

    public int getYear() {
        return year;
    }

    public Team getWinner() {
        return regionWinner;
    }

    /**
     * Command: Return every simulated game in this region.
     * Preconditions: Region result has been constructed.
     * Postconditions: Returns a flattened copy of the regional games by round.
     */
    public List<Game> collectGames() {
        List<Game> collectedGames = new ArrayList<>();
        collectedGames.addAll(fieldOf64);
        collectedGames.addAll(fieldOf32);
        collectedGames.addAll(sweet16);
        collectedGames.addAll(elite8);
        return collectedGames;
    }

    /**
     * Command: Serialize this region into a JSON string.
     * Preconditions: Region has been initialized.
     * Postconditions: Returns a JSON representation of the current region state.
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (IOException exc) {
            throw new IllegalStateException("Failed to serialize Region to JSON", exc);
        }
    }
}
