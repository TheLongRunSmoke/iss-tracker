package ru.tlrs.iss;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
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

    private Config() {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
    }

    public SharedPreferences getPreferences() {
        return mPreferences;
    }

    public boolean isLocationUseEnable(){
        return mPreferences.getBoolean(getKey(R.string.pref_use_location), true);
    }

    public boolean isUpdateLocation(){
        return Integer.parseInt(mPreferences.getString(getKey(R.string.pref_location_update), "0")) == 0;
    }

    public void setSavedLocation(Location location) {
        Editor editor = mPreferences.edit();
        editor.putString(getKey(R.string.pref_latitude), String.valueOf(location.getLatitude()));
        editor.putString(getKey(R.string.pref_longitude), String.valueOf(location.getLongitude()));
        editor.apply();
    }

    public Location getSavedLocation(){
        Location result = null;
        double latitude = Double.parseDouble(mPreferences.getString(getKey(R.string.pref_latitude), "0"));
        double longitude = Double.parseDouble(mPreferences.getString(getKey(R.string.pref_longitude), "0"));
        if (latitude != 0 && longitude != 0){
            result = new Location(android.location.LocationManager.NETWORK_PROVIDER);
            result.setLatitude(latitude);
            result.setLongitude(longitude);
        }
        return result;
    }

    private String getKey(int resId){
        return App.getAppContext().getResources().getString(resId);
    }
}
