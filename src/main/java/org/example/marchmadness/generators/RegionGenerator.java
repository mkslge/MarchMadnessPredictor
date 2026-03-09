package org.example.marchmadness.generators;

import org.example.marchmadness.factories.ModelFactory;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.storage.RegionStore;

public class RegionGenerator {
    private final RegionStore regionStore = new RegionStore();
    private final ModelFactory modelFactory = new ModelFactory();

    Region eastResult;
    Region midwestResult;
    Region southResult;
    Region westResult;

    public RegionGenerator(int year) {
        eastResult = modelFactory.createRegion(RegionType.EAST, year);
        midwestResult = modelFactory.createRegion(RegionType.MIDWEST, year);
        southResult = modelFactory.createRegion(RegionType.SOUTH, year);
        westResult = modelFactory.createRegion(RegionType.WEST, year);

        regionStore.save(RegionType.EAST, eastResult);
        regionStore.save(RegionType.MIDWEST, midwestResult);
        regionStore.save(RegionType.SOUTH, southResult);
        regionStore.save(RegionType.WEST, westResult);
    }

    public Region getEastResult() {
        return eastResult;
    }

    public Region getMidwestResult() {
        return midwestResult;
    }

    public Region getSouthResult() {
        return southResult;
    }

    public Region getWestResult() {
        return westResult;
    }
}
