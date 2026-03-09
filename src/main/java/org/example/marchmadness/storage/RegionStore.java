package org.example.marchmadness.storage;

import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;

import java.util.EnumMap;
import java.util.Map;

public class RegionStore {
    private final Map<RegionType, Region> regions = new EnumMap<>(RegionType.class);

    public void save(RegionType type, Region region) {
        regions.put(type, region);
    }

    public Region get(RegionType type) {
        return regions.get(type);
    }
}
