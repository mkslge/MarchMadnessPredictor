package org.example.marchmadness.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "teamyearstatistics")
public class TeamYearStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String team;
    private int year;
    private int seed;
    private double rating;

    private int bracketSimulations;
    private int bracketWins;
    private int gameSimulations;
    private int gameWins;

    /*
     * Pre-condition: Hibernate is loading a statistics row from the database.
     * Post-condition: Creates an empty statistics object for Hibernate to populate.
     *
     * Required by JPA for entity construction.
     */
    protected TeamYearStatistics() {
    }

    /*
     * Pre-condition: A team name, tournament year, and tournament seed are known.
     * Post-condition: Creates a statistics row with all tracked statistics initialized to 0.
     *
     * Constructs the base statistics record for one team's tournament year.
     */
    public TeamYearStatistics(String team, int year, int seed) {
        this.team = team;
        this.year = year;
        this.seed = seed;
        this.rating = 0;
        this.bracketSimulations = 0;
        this.bracketWins = 0;
        this.gameSimulations = 0;
        this.gameWins = 0;
    }

    public Long getId() {
        return id;
    }

    public String getTeam() {
        return team;
    }

    public int getYear() {
        return year;
    }

    public int getSeed() {
        return seed;
    }

    public double getRating() {
        return rating;
    }

    public int getBracketSimulations() {
        return bracketSimulations;
    }

    public int getBracketWins() {
        return bracketWins;
    }



    public int getGameSimulations() {
        return gameSimulations;
    }

    public int getGameWins() {
        return gameWins;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setBracketSimulations(int bracketSimulations) {
        this.bracketSimulations = bracketSimulations;
    }

    public void setBracketWins(int bracketWins) {
        this.bracketWins = bracketWins;
    }



    public void setGameSimulations(int gameSimulations) {
        this.gameSimulations = gameSimulations;
    }

    public void setGameWins(int gameWins) {
        this.gameWins = gameWins;
    }

}
