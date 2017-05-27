package tk.internet.praktikum.foursquare.api.bean;

import com.google.gson.annotations.SerializedName;

public enum Gender {
    @SerializedName("m")
    MALE,
    @SerializedName("f")
    FEMALE,
    @SerializedName("unspecified")
    UNSPECIFIED
}
