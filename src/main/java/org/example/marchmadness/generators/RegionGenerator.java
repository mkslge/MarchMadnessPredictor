package org.example.marchmadness.generators;

import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;

public class RegionGenerator {
    Region eastResult;
    Region midwestResult;
    Region southResult;
    Region westResult;
    public RegionGenerator(int year) {
        //create objects
        eastResult = new Region(RegionType.EAST, year);
        midwestResult = new Region(RegionType.MIDWEST, year);
        southResult = new Region(RegionType.SOUTH, year);
        westResult = new Region(RegionType.WEST, year);

        //run simulations
        eastResult.run();
        midwestResult.run();
        southResult.run();
        westResult.run();
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
