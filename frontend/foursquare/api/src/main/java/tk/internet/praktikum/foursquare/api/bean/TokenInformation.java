package tk.internet.praktikum.foursquare.api.bean;

import android.util.Base64;

import org.json.JSONObject;

public class TokenInformation {

    private String token;
    private String name;
    JSONObject jsonObject;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        name = token.split(".")[1];
        try {
            jsonObject = new JSONObject(new String(Base64.decode(name, Base64.DEFAULT)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        try {
            return jsonObject.getString("_id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
