package tk.internet.praktikum.foursquare.api.bean;

import android.os.Parcel;
import android.os.Parcelable;

import tk.internet.praktikum.foursquare.api.Constants;

public class Image implements Parcelable {
    private String _id;

    protected Image(Parcel in) {
        _id = in.readString();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    public String getId() {
        return _id;
    }

    @Override
    public int describeContents() {
        return Constants.IMAGE_PARCEL;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_id);
    }
}
