package tk.internet.praktikum.foursquare.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import tk.internet.praktikum.foursquare.api.bean.Comment;


class CommentDeserializer implements JsonDeserializer<Comment> {
    private final String kindElementName;
    private final Gson gson;
    private final Map<String, Class<? extends Comment>> kindRegistry;

    public CommentDeserializer(String kindElementName) {
        this.kindElementName = kindElementName;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
        kindRegistry = new HashMap<String, Class<? extends Comment>>();
    }

    public void registerComment(String kind, Class<? extends Comment> commentClass) {
        this.kindRegistry.put(kind, commentClass);
    }

    @Override
    public Comment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            JsonObject commentObject = json.getAsJsonObject();
            JsonElement commentKindElement = commentObject.get(kindElementName);
            Class<? extends Comment> commentClass = kindRegistry.get(commentKindElement.getAsString());
            Comment cmt = gson.fromJson(json, commentClass);
            return cmt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
