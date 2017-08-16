package tk.internet.praktikum.foursquare.api.bean;

import java.util.ArrayList;
import java.util.List;


public class Location  {
    private String type = "Point";
    private List<Double> coordinates;

    public Location(double longitude, double latitude) {
        type = "Point";
        coordinates = new ArrayList<>();
        coordinates.add(longitude);
        coordinates.add(latitude);
    }

    public Location() {
    }

    public Double getLongitude() {
        if (!isPoint())
            throw new UnsupportedOperationException();

        return coordinates.get(0);
    }

    public Double getLatitude() {
        if (!isPoint())
            throw new UnsupportedOperationException();

        return coordinates.get(1);
    }

    public boolean isPoint() {
        return type.equals("Point");
    }
}
