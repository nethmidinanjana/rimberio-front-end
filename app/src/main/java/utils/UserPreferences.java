package utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class UserPreferences {

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public UserPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUserData(String userId, String username, String email, String password) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        Log.i("rimberioLog", "User data saved successfully!");
        editor.apply();
    }

    public String getUserId(){
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getUsername(){
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getEmail(){
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public String getPassword(){
        return sharedPreferences.getString(KEY_PASSWORD, null);
    }

    public void clearUserData(){
        editor.clear();
        editor.apply();
    }
}
