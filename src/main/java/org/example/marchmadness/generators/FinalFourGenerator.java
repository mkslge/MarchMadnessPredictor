package org.example.marchmadness.generators;

import org.example.marchmadness.factories.ModelFactory;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.storage.FinalFourStore;

public class FinalFourGenerator {
    private final FinalFourStore finalFourStore = new FinalFourStore();
    private final ModelFactory modelFactory = new ModelFactory();

    public FinalFourGenerator(int year) {
        RegionGenerator rg = new RegionGenerator(year);
        finalFourStore.save(modelFactory.createFinalFour(
                rg.getEastResult(),
                rg.getMidwestResult(),
                rg.getSouthResult(),
                rg.getWestResult()
        ));
    }

    public FinalFour getResult() {
        return finalFourStore.get();
    }
}
