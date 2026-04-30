package org.example.marchmadness.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.marchmadness.simulation.winner.StochasticWinnerSelectionStrategy;
import org.example.marchmadness.simulation.winner.WinnerSelectionStrategy;

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
    private final WinnerSelectionStrategy winnerSelectionStrategy;

    public Bracket(int year) {
        this(year, new StochasticWinnerSelectionStrategy());
    }

    /**
     * Command: Create a bracket with an explicit winner selection strategy.
     * Preconditions: Winner selection strategy is non-null.
     * Postconditions: Bracket is initialized and ready to run.
     */
    public Bracket(int year, WinnerSelectionStrategy winnerSelectionStrategy) {
        if (winnerSelectionStrategy == null) {
            throw new IllegalArgumentException("Winner selection strategy cannot be null");
        }
        this.year = year;
        this.winnerSelectionStrategy = winnerSelectionStrategy;
    }

    /**
     * Command: Run a full tournament simulation for this bracket year.
     * Preconditions: Year is valid and all region datasets exist.
     * Postconditions: Regions, Final Four, and champion are populated.
     */
    public void run() {
        initRegions();
        finalFour = new FinalFour(regions.get(0), regions.get(1), regions.get(2), regions.get(3), winnerSelectionStrategy);
        champion = finalFour.getChampion();
    }

    /**
     * Command: Initialize regional simulations in fixed tournament order.
     * Preconditions: Bracket year is set.
     * Postconditions: `regions` contains EAST, MIDWEST, SOUTH, WEST simulations.
     */
    private void initRegions() {
        regions.clear();
        regions.add(new Region(RegionType.EAST, year, winnerSelectionStrategy));
        regions.add(new Region(RegionType.MIDWEST, year, winnerSelectionStrategy));
        regions.add(new Region(RegionType.SOUTH, year, winnerSelectionStrategy));
        regions.add(new Region(RegionType.WEST, year, winnerSelectionStrategy));
    }

    public String getChampionName() {
        return finalFour.getChampion().getName();
    }

    public Team getChampion() {
        return finalFour.getChampion();
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
     * Preconditions: Bracket simulation has been run.
     * Postconditions: Returns all regional, semifinal, and championship games.
     */
    public List<Game> collectGames() {
        List<Game> collectedGames = new ArrayList<>();
        for (Region region : regions) {
            collectedGames.addAll(region.collectGames());
        }

        if (finalFour != null) {
            collectedGames.add(finalFour.getSouthVSWest());
            collectedGames.add(finalFour.getEastVSMidwest());
            collectedGames.add(finalFour.getChampionship());
        }

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
