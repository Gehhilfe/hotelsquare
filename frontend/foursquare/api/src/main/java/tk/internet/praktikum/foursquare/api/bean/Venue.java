package tk.internet.praktikum.foursquare.api.bean;

public class Venue {

    String name;
    float longitude;
    float latitude;

    public Venue(){

    };

    public Venue(String name, float longitude, float latitude)
    {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    void setName(String name)
    {
        this.name = name;
    }

    String getName()
    {
        return name;
    }

    void setLongitude(float longitude)
    {
        this.longitude = longitude;
    }

    float getLongitude()
    {
        return longitude;
    }

    void setLatitude()
    {
        this.latitude = latitude;
    }

    float getLatitude()
    {
        return latitude;
    }

}
