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

    public FinalFour(Region east, Region midwest, Region south, Region west) {
        this.east = new Team(east.getWinner());
        this.midwest = new Team(midwest.getWinner());
        this.south = new Team(south.getWinner());
        this.west = new Team(west.getWinner());

        this.southVSWest = new Game(this.south, this.west);
        this.eastVSMidwest = new Game(this.east, this.midwest);
        this.championship = new Game(southVSWest.getWinner(), eastVSMidwest.getWinner());
        this.champion = championship.getWinner();
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

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (IOException exc) {
            throw new IllegalStateException("Failed to serialize FinalFour to JSON", exc);
        }
    }
}
