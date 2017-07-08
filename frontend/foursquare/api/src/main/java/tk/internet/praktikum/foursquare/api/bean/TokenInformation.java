package tk.internet.praktikum.foursquare.api.bean;

import android.util.Base64;

import com.google.gson.Gson;

import org.json.JSONObject;

public class TokenInformation {

    private String token;
    private User user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        String payload = token.split(".")[1];
        try {
            Gson gson = new Gson();
            user = gson.fromJson(new String(Base64.decode(payload, Base64.DEFAULT)), User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        try {
            return user;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setUser(User value) {
        user = value;
    }
}
