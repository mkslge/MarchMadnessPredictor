package org.example.marchmadness.simulation;

public final class BracketSimulatorFactory {
    private BracketSimulatorFactory() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Command: Resolve a concrete bracket simulator for a simulation mode.
     * Preconditions: `mode` is non-null.
     * Postconditions: Returns a simulator implementation matching `mode`.
     */
    public static BracketSimulator create(SimulationMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Simulation mode cannot be null");
        }

        return switch (mode) {
            case DETERMINISTIC -> new DeterministicBracketSimulator();
            case STOCHASTIC -> new StochasticBracketSimulator();
        };
    }
}
