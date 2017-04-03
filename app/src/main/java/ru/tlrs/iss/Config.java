package ru.tlrs.iss;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class Config {

    private static volatile Config sInstance;

    private static SharedPreferences mPreferences;

    public static Config getInstance() {
        Config localInstance = sInstance;
        if (localInstance == null){
            synchronized (Config.class){
                localInstance = sInstance;
                if (localInstance == null){
                    localInstance = sInstance = new Config();
                }
            }
        }
        return localInstance;
    }

    public Config() {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
    }

    public SharedPreferences getPreferences() {
        return mPreferences;
    }
}
