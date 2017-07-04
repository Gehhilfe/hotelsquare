package tk.internet.praktikum.foursquare.api.service;

import java.net.URISyntaxException;
import java.util.List;

import io.reactivex.Observable;
import io.socket.client.IO;
import io.socket.client.Socket;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import tk.internet.praktikum.foursquare.api.bean.Chat;
import tk.internet.praktikum.foursquare.api.bean.Message;

public interface ChatService {

    /**
     * Pushes a chat to a dedicated recipient
     *
     * @param recipients receiver of the message of the newly generated Chat
     * @return chatId and status message ("New Chat")
     */
    @POST("chat/{recipients}")
    Observable<Object> newChat(@Path("recipients") String recipients, @Body Message message);

    /**
     * Replies to an old message
     *
     * @param chatId id of chat, where the message is published
     * @return status message ("replied to message")
     */
    @POST("chat/reply/{chatId}")
    Observable<Object> replyMessage(@Path("chatId") String chatId, @Body Message message);

    /**
     * Retrieves chat of currently authenticated user with id chatId
     *
     * @param chatId id of chat
     * @return chat history of passed ID
     */
    @GET("chat/with/{chatId}")
    Observable<Chat> getConversation(@Path("chatId") String chatId);

    /**
     * Retrieves chat of currently authenticated user with id chatId
     *
     * @return all chats with the last message as teaser (you can get more if you want... ;)) for the authenticated user
     */
    @GET("chat/all")
    Observable<List<Chat>> getConversations();
}
