package tk.internet.praktikum.foursquare.api.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Chat {

    @SerializedName("_id")
    private String chatId;
    private List<User> participants;
    private List<ChatMessage> messages;

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

    public List<ChatMessage> getMessages() {
        return messages;
    }
}
