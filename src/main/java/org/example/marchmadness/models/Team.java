package org.example.marchmadness.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;

public class Team {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private int year;

    @JsonProperty("Seed")
    private int seed;

    @JsonProperty("Team")
    private String name;

    @JsonProperty("AdjT")
    private double adjT;

    @JsonProperty("AdjEM")
    private double adjEM;

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
        this.name = name;
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
        this.name = other.name;
        this.seed = other.seed;
        this.adjEM = other.adjEM;
        this.adjT = other.adjT;
    }

    void setTeam(Team other) {
        if (other == null) {
            return;
        }

        this.name = other.name;
        this.seed = other.seed;
        this.adjEM = other.adjEM;
        this.adjT = other.adjT;
    }

    public int getYear() {
        return this.year;
    }

    public String getName() {
        return this.name;
    }

    public int getSeed() {
        return this.seed;
    }

    public double getAdjT() {
        return this.adjT;
    }

    public double getAdjEM() {
        return this.adjEM;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (IOException exc) {
            throw new IllegalStateException("Failed to serialize Team to JSON", exc);
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Team otherTeam)) {
            return false;
        }
        return Objects.equals(otherTeam.name, this.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
