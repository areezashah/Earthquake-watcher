package util;

import java.util.Random;

public class Constants {
    public static final String URL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_week.geojson";
    public static final int LIMIT = 100;           //there are lot of earthquakes ut we will show only 30

    public static int randomInt(int min, int max){
        return new Random().nextInt(max-min)+min;
    }
}
