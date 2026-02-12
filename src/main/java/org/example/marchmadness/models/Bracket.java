package org.example.marchmadness.models;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.util.ArrayList;

public class Bracket {

    @JsonProperty
    int year;

    @JsonProperty
    Team champion;

    @JsonProperty
    FinalFour finalFour;

    @JsonProperty
    ArrayList<Region> regions = new ArrayList<>();
    String json;

    public Bracket(int year)  {
        this.year = year;

    }

    public void run() {
        initRegions();
        finalFour = new FinalFour(regions.get(0), regions.get(1), regions.get(2), regions.get(3));
        champion = finalFour.getChampion();
    }

    private void initRegions()  {
        regions.add(new Region(RegionType.EAST, year));
        regions.add(new Region(RegionType.MIDWEST, year));
        regions.add(new Region(RegionType.SOUTH, year));
        regions.add(new Region(RegionType.WEST, year));
    }

    public String getChampionName() {
        return finalFour.getChampion().name;
    }

    public Team getChampion() {
        return finalFour.getChampion();
    }



    public String toJson() {
        if(json != null) {
            return json;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString;
            try {
                json = mapper.writeValueAsString(this);
            } catch(IOException exc) {
                exc.printStackTrace();
                return null;
            }
            return json;
        }
    }




}
