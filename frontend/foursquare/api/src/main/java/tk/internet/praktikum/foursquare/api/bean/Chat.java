package tk.internet.praktikum.foursquare.api.bean;

import java.util.List;

public class Chat {

    String chatId;

    List<User> participants;

    public Chat(String chatId, List<User> participants)
    {
        this.chatId = chatId;
        this.participants = participants;
    }

    void setChatId(String chatId)
    {
        this.chatId = chatId;
    }

    void setParticipants(List<User> participants)
    {
        this.participants = participants;
    }

    String getChatId()
    {
        return chatId;
    }

    List<User> getParticipants()
    {
        return participants;
    }
}
