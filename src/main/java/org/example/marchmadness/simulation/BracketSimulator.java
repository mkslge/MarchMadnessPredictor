package org.example.marchmadness.simulation;

import org.example.marchmadness.models.Bracket;

public interface BracketSimulator {

    /**
     * Command: Simulate a bracket for the requested year.
     * Preconditions: Year datasets exist and are readable.
     * Postconditions: Returns a completed bracket simulation for `year`.
     */
    Bracket simulate(int year);

    /**
     * Command: Report the simulation mode handled by this simulator.
     * Preconditions: None.
     * Postconditions: Returns the simulator mode identifier.
     */
    SimulationMode getMode();
}
