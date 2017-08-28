package tk.internet.praktikum.foursquare.api.bean;

import android.util.Base64;

import com.google.gson.Gson;

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
            String json = new String(Base64.decode(payload, Base64.DEFAULT));
            user = gson.fromJson(json, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        try {
            if(user == null) {
                String[] splited = token.split("[.]");
                String payload = splited[1];
                try {
                    Gson gson = new Gson();
                    String json = new String(Base64.decode(payload, Base64.DEFAULT));
                    user = gson.fromJson(json, User.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
