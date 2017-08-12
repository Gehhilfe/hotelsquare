package tk.internet.praktikum.foursquare.api.service;

import java.net.URISyntaxException;
import java.util.List;

import io.reactivex.Observable;
import io.socket.client.IO;
import io.socket.client.Socket;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import tk.internet.praktikum.foursquare.api.bean.Chat;
import tk.internet.praktikum.foursquare.api.bean.ChatMessage;
import tk.internet.praktikum.foursquare.api.bean.Message;
import tk.internet.praktikum.foursquare.api.bean.RecipientMessage;

public interface ChatService {

    /**
     * Pushes a chat to a dedicated recipient
     *
     * @param message First message including all recipients
     * @return chatId and status message ("New Chat")
     */
    @POST("chats")
    Observable<Chat> newChat(@Body RecipientMessage message);

    /**
     * Replies to an old message
     *
     * @param chatId id of chat, where the message is published
     * @return status message ("replied to message")
     */
    @POST("chats/{chatId}/messages")
    Observable<ChatMessage> replyMessage(@Path("chatId") String chatId, @Body Message message);

    /**
     * Retrieves chat of currently authenticated user with id chatId
     *
     * @param chatId id of chat
     * @return chat history of passed ID
     */
    @GET("chats/{chatId}")
    Observable<Chat> getConversation(@Path("chatId") String chatId);

    /**
     * Retrieves chat of currently authenticated user with id chatId
     *
     * @param chatId id of chat
     * @return chat history of passed ID
     */
    @GET("chats/{chatId}/{page}")
    Observable<Chat> getConversation(@Path("chatId") String chatId, @Path("page") int page);

    /**
     * Retrieves chat of currently authenticated user with id chatId but only messages that are newer than provided message id
     * Should be used to poll for new messages
     *
     * @param chatId id of chat
     * @return chat history of passed ID
     */
    @GET("chats/{chatId}")
    Observable<Chat> getConversation(@Path("chatId") String chatId, @Query("lastMessage") String lastMessageID);

    /**
     * Retrieves chat of currently authenticated user with id chatId
     *
     * @return all chats with the last message as teaser (you can get more if you want... ;)) for the authenticated user
     */
    @GET("chats")
    Observable<List<Chat>> getConversations();
}
