package org.example.marchmadness.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class Team {
    int year;

    @JsonProperty("Seed")
    int seed;

    @JsonProperty("Team")
    String name;

    @JsonProperty("AdjT")
    double adjT;

    @JsonProperty("AdjEM")
    double adjEM;

    String json;

    Team(String name, int seed) {
        this.name = name;
        this.seed = seed;
    }
    Team(String name, int seed, double adjEM, double adjT) {
        this.name = name;
        this.seed = seed;
        this.adjEM = adjEM;
        this.adjT = adjT;
    }

    public Team(int year, String name, int seed) {
        this.year = year;
        this.name = new String(name);
        this.seed = seed;

    }

    public Team() {
        this.name = "";
    }
    public Team(int year) {
        this.name = "";
        this.year = year;
    }





    public Team(Team other) {
        if (other == null) {
            throw new IllegalArgumentException("The provided team cannot be null");
        }
        this.year = other.year;
        this.name =  other.name;
        this.seed = other.seed;
        this.adjEM = other.adjEM;
        this.adjT = other.adjT;
        this.json = other.json;
    }

    void setTeam(Team other) {
        if(other == null) {
            return;
        }

        this.name = other.name;
        this.seed = other.seed;
        this.adjEM = other.adjEM;
        this.adjT = other.adjT;
    }


    int getYear() {
        return this.year;
    }

    String getName() {
        return this.name;
    }


    public int getSeed() {
        return this.seed;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String toJson() {
        if(json != null) {
            return json;
        }
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
    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Team otherTeam)) {
            return false;
        }
        return otherTeam.name.equals(this.name);
    }

}
