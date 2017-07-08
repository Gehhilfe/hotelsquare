package tk.internet.praktikum.foursquare.api.service;

import android.os.Bundle;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import tk.internet.praktikum.foursquare.api.bean.TokenInformation;

public class NotificationService {


    public Socket _socket;

    {
        try {
            _socket = IO.socket("https://dev.ip.stimi.ovh/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void connect(TokenInformation tokenInformation) {
        _socket.connect();
        _socket.on(tokenInformation.getUser().getId(), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                String type;
                try{
                    type = data.getString("type");
                } catch (Exception e){
                    e.printStackTrace();
                    return;
                }
                switch (type){
                    case "message": {

                        break;
                    }
                    case "friend_location": {

                        break;
                    }
                    case "friend": {

                        break;
                    }

                }

            }
        });
    }

    public void disconnect(){
        _socket.disconnect();
    }

    public void on() {

    }

}


