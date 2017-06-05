package ru.tlrs.iss;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;

import javax.inject.Inject;

public final class Config {

    @Inject
    Context context;

    @Inject
    SharedPreferences preferences;

    public Config() {
        App.getComponent().inject(this);
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public boolean isLocationUseEnable(){
        return preferences.getBoolean(getKey(R.string.pref_use_location), true);
    }

    public boolean isUpdateLocationOnStartup(){
        return Integer.parseInt(preferences.getString(getKey(R.string.pref_location_update), "0")) == 0;
    }

    public void setSavedLocation(Location location) {
        Editor editor = preferences.edit();
        editor.putString(getKey(R.string.pref_latitude), String.valueOf(location.getLatitude()));
        editor.putString(getKey(R.string.pref_longitude), String.valueOf(location.getLongitude()));
        editor.apply();
    }

    public Location getSavedLocation(){
        Location result = null;
        double latitude = Double.parseDouble(preferences.getString(getKey(R.string.pref_latitude), "0"));
        double longitude = Double.parseDouble(preferences.getString(getKey(R.string.pref_longitude), "0"));
        if (latitude != 0 || longitude != 0){
            result = new Location(android.location.LocationManager.NETWORK_PROVIDER);
            result.setLatitude(latitude);
            result.setLongitude(longitude);
        }
        return result;
    }

    public boolean isSavedLocationZero(){
        double latitude = Double.parseDouble(preferences.getString(getKey(R.string.pref_latitude), "0"));
        double longitude = Double.parseDouble(preferences.getString(getKey(R.string.pref_longitude), "0"));
        return latitude == 0 && longitude == 0;
    }

    public void setLocationUpdateOnStartup(boolean isEnabled) {
        Editor editor = preferences.edit();
        editor.putString(getKey(R.string.pref_location_update), String.valueOf(isEnabled ? 0 : 1));
        editor.apply();
    }

    private String getKey(int resId){
        return context.getResources().getString(resId);
    }
}
