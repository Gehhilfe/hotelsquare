package tk.internet.praktikum.foursquare.api.bean;

import java.util.ArrayList;
import java.util.List;


public class Location {
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        else {
            if (obj instanceof Location) {
                Location location = (Location) obj;
                return (this.getLatitude() == location.getLatitude() && this.getLongitude() == location.getLongitude());
            }
            return false;
        }

    }
}
