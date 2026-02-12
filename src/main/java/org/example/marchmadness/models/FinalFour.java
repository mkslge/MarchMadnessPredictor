package org.example.marchmadness.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class FinalFour {
    @JsonProperty
    private Team west;
    @JsonProperty
    private Team east;
    @JsonProperty
    private Team south;
    @JsonProperty
    private Team midwest;
    private String json;

    private Game southVSWest;
    private Game eastVSMidwest;
    private Game championship;

    private Team champion;

    public FinalFour(Region east, Region midwest, Region south, Region west) {

        this.east = new Team(east.getWinner());
        this.midwest = new Team(midwest.getWinner());
        this.south = new Team(south.getWinner());
        this.west = new Team(west.getWinner());




        southVSWest = new Game(this.south, this.west);
        eastVSMidwest = new Game(this.east, this.midwest);
        championship = new Game(southVSWest.getWinner(), eastVSMidwest.getWinner());
        champion = championship.getWinner();
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


    public void setChampion(Team champion) {
        this.champion = champion;
    }

    public String toJson() {
        if(json != null) {
            return json;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString;
            try {
                json = mapper.writeValueAsString(this);
            } catch(IOException exc) {
                exc.printStackTrace();
                return null;
            }
            return json;
        }
    }
}
