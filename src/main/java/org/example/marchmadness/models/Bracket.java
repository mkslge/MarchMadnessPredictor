package org.example.marchmadness.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Bracket {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int EXPECTED_REGION_COUNT = 4;

    @JsonProperty
    private final int year;

    @JsonProperty
    private final Team champion;

    @JsonProperty
    private final FinalFour finalFour;

    @JsonProperty
    private final List<Region> regions;

    /**
     * Command: Create a completed bracket result.
     * Preconditions: Regions contains four completed region results and Final Four is non-null.
     * Postconditions: Bracket stores the tournament year, regions, Final Four, and champion.
     */
    public Bracket(int year, List<Region> regions, FinalFour finalFour) {
        if (regions == null || finalFour == null) {
            throw new IllegalArgumentException("Bracket result values cannot be null");
        }
        if (regions.size() != EXPECTED_REGION_COUNT) {
            throw new IllegalArgumentException("Bracket requires exactly four regions");
        }
        this.year = year;
        this.regions = List.copyOf(regions);
        this.finalFour = finalFour;
        this.champion = new Team(finalFour.getChampion());
    }

    public String getChampionName() {
        return champion.getName();
    }

    public Team getChampion() {
        return champion;
    }

    /**
     * Command: Return the tournament year represented by this bracket.
     * Preconditions: Bracket has been constructed.
     * Postconditions: Returns the immutable year for this bracket.
     */
    public int year() {
        return year;
    }

    /**
     * Command: Return every simulated game in this bracket.
     * Preconditions: Bracket result has been constructed.
     * Postconditions: Returns all regional, semifinal, and championship games.
     */
    public List<Game> collectGames() {
        List<Game> collectedGames = new ArrayList<>();
        for (Region region : regions) {
            collectedGames.addAll(region.collectGames());
        }

        collectedGames.add(finalFour.getSouthVSWest());
        collectedGames.add(finalFour.getEastVSMidwest());
        collectedGames.add(finalFour.getChampionship());

        return collectedGames;
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
