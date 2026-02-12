package org.example.marchmadness.models;

public enum RegionType {
    EAST("east"),
    MIDWEST("midwest"),
    SOUTH("south"),
    WEST("west");

    private final String regionName;

    RegionType(String type) {
        regionName = type;
    }

    public String toString() {
        return this.regionName;
    }

}
