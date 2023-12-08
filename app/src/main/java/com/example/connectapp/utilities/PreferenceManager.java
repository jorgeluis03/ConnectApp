package com.example.connectapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;
public class PreferenceManager {
    /*SharedPreferences se utiliza comúnmente para almacenar configuraciones de la aplicación, preferencias del usuario,
    tokens de acceso, información de inicio de sesión, y otros datos que deben sobrevivir a la terminación y reinicio de la aplicación.*/
    private final SharedPreferences sharedPreferences;
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME,Context.MODE_PRIVATE);
    }

    public void putBoolean(String key, Boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }

    public boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key,false);
    }

    public void putString(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }

    public String getString(String key){
        return sharedPreferences.getString(key,null);
    }

    public void clear(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
