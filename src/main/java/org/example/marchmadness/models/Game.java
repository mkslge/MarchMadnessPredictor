package org.example.marchmadness.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Random;
import static java.lang.Math.sqrt;

public class Game {
    int standardDeviation = 11;
    Team team1;
    Team team2;
    @JsonProperty
    Team winner;
    @JsonProperty
    Team loser;

    String json;

    double oddsOutOf100;

    public Game (String name1, int seed1, double adjEM1, double adjT1,
          String name2, int seed2, double adjEM2, double adjT2) {
        team1 = new Team(name1, seed1, adjEM1, adjT1);
        team2 = new Team(name2, seed2, adjEM2, adjT2);
        this.calculateOdds();
        this.calculateWinner();
    }
    public Game(Team team1, Team team2) {
        this.team1 = team1;
        this.team2 = team2;
        this.calculateOdds();
        this.calculateWinner();

    }

   public Game (String name1, int seed1, String name2, int seed2) {
        team1 = new Team(name1, seed1);
        team2 = new Team(name2, seed2);
    }

    Game() {
        team1 = new Team();
        team2 = new Team();
        winner = null;
        loser = null;
    }
    public Game(int year) {
        team1 = new Team(year);
        team2 = new Team(year);
        winner = null;
        loser = null;
    }
    public double calculateOdds() {
        double pointDiff = (team1.adjEM - team2.adjEM) * (team1.adjT + team2.adjT) / 200;
        double CDF = 0.5 * (1 + erf((0 - pointDiff)/(standardDeviation * sqrt(2))));
        oddsOutOf100 = CDF * 100;
        return oddsOutOf100;
    }
    public String calculateWinner() {
        Random random = new Random();

        double randomDouble = (100) * random.nextDouble();
        if(oddsOutOf100 < randomDouble) {
            winner = new Team(team1);
            loser = new Team(team2);
        } else {
            winner = new Team(team2);
            loser = new Team(team1);
        }
        return winner.name;
    }
    public void addTeams(Team team1, Team team2) {
        this.team1 = team1;
        this.team2 = team2;
    }
    public void printGame() {
        System.out.println("Game: " + team1.name + " v.s " + team2.name);
    }

    public void setTeam1(Team other) {
        team1 = new Team(other);
    }

    public void setTeam2(Team other) {
        team2 = new Team(other);
    }

    public Team getWinner() {
        return this.winner;
    }

    public Team getLoser() {
        return this.loser;
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

    public String toString() {
        return team1.toString() +
                " vs " +
                team2.toString();
    }


    private static double erf(double x) {
        // Constants
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;

        // Save the sign of x
        int sign = 1;
        if (x < 0)
            sign = -1;
        x = Math.abs(x);

        // A&S formula 7.1.26
        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

        return sign * y;
    }




}
