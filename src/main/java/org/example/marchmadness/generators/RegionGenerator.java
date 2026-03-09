package org.example.marchmadness.generators;

import org.example.marchmadness.factories.ModelFactory;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.storage.RegionStore;

public class RegionGenerator {
    private final RegionStore regionStore = new RegionStore();
    private final ModelFactory modelFactory = new ModelFactory();

    /**
     * Command: Generate and store all four regional simulations for a year.
     * Preconditions: Year datasets exist for all regions.
     * Postconditions: Region store contains EAST, MIDWEST, SOUTH, and WEST results.
     */
    public RegionGenerator(int year) {
        regionStore.save(RegionType.EAST, modelFactory.createRegion(RegionType.EAST, year));
        regionStore.save(RegionType.MIDWEST, modelFactory.createRegion(RegionType.MIDWEST, year));
        regionStore.save(RegionType.SOUTH, modelFactory.createRegion(RegionType.SOUTH, year));
        regionStore.save(RegionType.WEST, modelFactory.createRegion(RegionType.WEST, year));
    }

    public Region getEastResult() {
        return regionStore.get(RegionType.EAST);
    }

    public Region getMidwestResult() {
        return regionStore.get(RegionType.MIDWEST);
    }

    public Region getSouthResult() {
        return regionStore.get(RegionType.SOUTH);
    }

    public Region getWestResult() {
        return regionStore.get(RegionType.WEST);
    }
}
