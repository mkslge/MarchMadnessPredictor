package org.example.marchmadness.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class FinalFour {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonProperty
    private final Team west;

    @JsonProperty
    private final Team east;

    @JsonProperty
    private final Team south;

    @JsonProperty
    private final Team midwest;

    private final Game southVSWest;
    private final Game eastVSMidwest;
    private final Game championship;
    private final Team champion;

    /**
     * Command: Create a completed Final Four result.
     * Preconditions: All teams, semifinal games, championship game, and champion are non-null.
     * Postconditions: Final Four stores participants, games, and champion result.
     */
    public FinalFour(
            Team east,
            Team midwest,
            Team south,
            Team west,
            Game southVSWest,
            Game eastVSMidwest,
            Game championship,
            Team champion
    ) {
        if (east == null
                || midwest == null
                || south == null
                || west == null
                || southVSWest == null
                || eastVSMidwest == null
                || championship == null
                || champion == null) {
            throw new IllegalArgumentException("Final Four result values cannot be null");
        }
        this.east = new Team(east);
        this.midwest = new Team(midwest);
        this.south = new Team(south);
        this.west = new Team(west);
        this.southVSWest = southVSWest;
        this.eastVSMidwest = eastVSMidwest;
        this.championship = championship;
        this.champion = new Team(champion);
    }

    public Game getChampionship() {
        return championship;
    }

    public Game getEastVSMidwest() {
        return eastVSMidwest;
    }

    public Game getSouthVSWest() {
        return southVSWest;
    }

    public Team getChampion() {
        return champion;
    }

    public Team getEast() {
        return east;
    }

    public Team getMidwest() {
        return midwest;
    }

    public Team getSouth() {
        return south;
    }

    public Team getWest() {
        return west;
    }

    /**
     * Command: Serialize this Final Four into a JSON string.
     * Preconditions: Final Four state is initialized.
     * Postconditions: Returns a JSON representation of the current Final Four.
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (IOException exc) {
            throw new IllegalStateException("Failed to serialize FinalFour to JSON", exc);
        }
    }
}
