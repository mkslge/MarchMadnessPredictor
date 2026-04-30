package org.example.marchmadness.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table (name = "season")
public class Season {
    @Id
    long year;
    int simulations;


    protected Season() {
    }

    public Season(int year) {
        this.year = year;
        this.simulations = 0;
    }

    public Season(long year, int simulations) {
        this.year = year;
        this.simulations = simulations;
    }

    public long getYear() {
        return year;
    }

    public int getSimulations() {
        return simulations;
    }

    public void addSimulation() {
        simulations++;
    }
}
