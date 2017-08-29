package tk.internet.praktikum.foursquare.storage;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.TokenInformation;
import tk.internet.praktikum.foursquare.api.bean.User;


public class LocalStorage {

    private static final String LOG_TAG = LocalStorage.class.getSimpleName();
    private static LocalStorage localStorage;
    private static SharedPreferences sharedPreferences;
    private static Context context;

    /**
     * @return
     */
    public static LocalStorage getLocalStorageInstance(Context context) {
        if (LocalStorage.context == null) {
            LocalStorage.context = context;
            LocalStorage.sharedPreferences = getSharedPreferences(context);
        }
        return localStorage != null ? localStorage : new LocalStorage();
    }


    /**
     * @return
     */
    private SharedPreferences.Editor getEditor() {
        return sharedPreferences.edit();
    }

    /**
     * @param context
     * @return
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getString(R.string.user_preferences), Context.MODE_PRIVATE);
    }


    /**
     * It's called when logging in
     *
     * @param tokenInformation
     */
    public void saveToken(TokenInformation tokenInformation) {
        saveBooleanValue(Constants.IS_LOGGED_IN, true);
        saveValue(Constants.TOKEN, tokenInformation.getToken());
    }

    public void saveBooleanValue(String key, boolean value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * @param key
     * @param value
     */
    public void saveValue(String key, String value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * @param tokenInformation
     * @param user
     */
    public void saveLoggedinInformation(TokenInformation tokenInformation, User user) {
        saveToken(tokenInformation);
        savedUserInformation(user);
    }

    /**
     * @param user
     */
    public void savedUserInformation(User user) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(Constants.NAME, user.getName());
        editor.putString(Constants.EMAIL, user.getEmail());
        editor.commit();
    }

    /**
     * @param key
     */
    public void deleteValue(String key) {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(key);
        editor.commit();
    }

    public void deleteToken() {
        deleteValue(Constants.TOKEN);
    }

    /**
     * It's called when logging out.
     */
    public void deleteLoggedInInformation() {
        SharedPreferences.Editor editor = getEditor();
        deleteValue(Constants.IS_LOGGED_IN);
        deleteValue(Constants.EMAIL);
        deleteValue(Constants.NAME);
        deleteValue(Constants.TOKEN);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.IS_LOGGED_IN, false);
    }
    public boolean alreadyReadStaticKeyWords() {
        return sharedPreferences.getBoolean(Constants.ALREADY_READ_STATIC_KEYWORDS,false);
    }


    /**
     * @param key
     * @param value
     */
    public void setLanguage(String key, String value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(key, value);
        editor.commit();
    }

    /** Stores the chat id and the date of the last read message.
     * @param chatId Id of the chat.
     * @param date Date of the last read message.
     */
    public void saveChatDate(String chatId, Date date) {
        SharedPreferences.Editor editor = getEditor();
        editor.putLong(chatId, date.getTime());
        editor.commit();
    }

}
