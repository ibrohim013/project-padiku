package com.jendral.padiku;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class preferences {
    private static final String KEY_DATA = "key_data";
    private static final String NAMA_DATA = "nama_data";
    private static final String LEVEL_DATA = "level_data";
    private static final String ACTIVE_DATA = "active_data";

    private static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setKeyData(Context context, String s){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_DATA,s);
        editor.apply();
    }

    public static String getKeyData(Context context){
        return getSharedPreferences(context).getString(KEY_DATA,"");
    }


    public static void setNamaData(Context context,String s){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(NAMA_DATA,s);
        editor.apply();
    }

    public static String getNamaData(Context context){
        return getSharedPreferences(context).getString(NAMA_DATA,"");
    }

    public static void setLevelData(Context context,String s){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(LEVEL_DATA,s);
        editor.apply();
    }

    public static String getLevelData(Context context){
        return getSharedPreferences(context).getString(LEVEL_DATA,"");
    }

    public static void setActiveData(Context context, boolean isActive){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(ACTIVE_DATA,isActive);
        editor.apply();
    }

    public static boolean getActiveData(Context context){
        return getSharedPreferences(context).getBoolean(ACTIVE_DATA,false);
    }


    public static void clearData(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_DATA);
        editor.remove(NAMA_DATA);
        editor.remove(ACTIVE_DATA);
        editor.remove(LEVEL_DATA);
        editor.apply();
    }

}
