package org.example.marchmadness.generators;

import org.example.marchmadness.factories.ModelFactory;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.storage.BracketStore;

public class BracketGenerator {
    private final BracketStore bracketStore = new BracketStore();
    private final ModelFactory modelFactory = new ModelFactory();
    private final int year;

    /**
     * Command: Generate and store a full bracket simulation for a year.
     * Preconditions: Year datasets exist for all tournament regions.
     * Postconditions: Bracket store contains a completed bracket for `year`.
     */
    public BracketGenerator(int year) {
       this.year = year;
       Bracket bracket = modelFactory.createBracket(year);
       bracket.run();
       bracketStore.save(year, bracket);
    }

    public Bracket getBracket() {
        return bracketStore.get(year);
    }
}
