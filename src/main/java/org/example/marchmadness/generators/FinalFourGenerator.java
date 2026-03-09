package org.example.marchmadness.generators;

import org.example.marchmadness.factories.ModelFactory;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.storage.FinalFourStore;

public class FinalFourGenerator {
    private final FinalFourStore finalFourStore = new FinalFourStore();
    private final ModelFactory modelFactory = new ModelFactory();

    /**
     * Command: Generate and store a Final Four simulation for a year.
     * Preconditions: Year datasets exist for all regions.
     * Postconditions: Final Four store contains semifinal, final, and champion results.
     */
    public FinalFourGenerator(int year) {
        RegionGenerator regionGenerator = new RegionGenerator(year);
        finalFourStore.save(modelFactory.createFinalFour(
                regionGenerator.getEastResult(),
                regionGenerator.getMidwestResult(),
                regionGenerator.getSouthResult(),
                regionGenerator.getWestResult()
        ));
    }

    public FinalFour getResult() {
        return finalFourStore.get();
    }
}
