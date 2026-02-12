package org.example.marchmadness.generators;

import org.example.marchmadness.models.FinalFour;

public class FinalFourGenerator {
    private FinalFour result;
    public FinalFourGenerator(int year) {
        RegionGenerator rg = new RegionGenerator(year);
        result = new FinalFour(rg.eastResult, rg.midwestResult, rg.southResult, rg.westResult);
    }
}
