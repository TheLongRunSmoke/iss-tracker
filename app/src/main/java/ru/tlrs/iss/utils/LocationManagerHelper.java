package ru.tlrs.iss.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import java.util.Locale;

import ru.tlrs.iss.App;
import timber.log.Timber;

public final class LocationManagerHelper implements LocationListener{

    private static volatile LocationManagerHelper sInstance;

    private static final String LOCATION_MANAGER = android.location.LocationManager.NETWORK_PROVIDER;
    private final LocationManager mLocationManager;
    private Location mCurrentLocation;
    private OnLocationChangeListener mListener;

    public static LocationManagerHelper getInstance() {
        LocationManagerHelper localInstance = sInstance;
        if (localInstance == null) {
            synchronized (LocationManagerHelper.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new LocationManagerHelper();
                }
            }
        }
        return localInstance;
    }

    private LocationManagerHelper() {
        mLocationManager = (LocationManager) App.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(App.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mCurrentLocation = mLocationManager.getLastKnownLocation(LOCATION_MANAGER);
            mLocationManager.requestLocationUpdates(LOCATION_MANAGER, 0, 0, this);
        }
    }

    public Location getCurrentLocation() {
        if (mCurrentLocation != null){
            Timber.d(String.format(Locale.ENGLISH, "getCurrentLocation(): location: lat = %f, long = %f", mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        }else {
            Timber.d("getCurrentLocation(): null");
        }
        return mCurrentLocation;
    }

    public void setLocationChangeListener(OnLocationChangeListener listener){
        this.mListener = listener;
    }

    @Override
    public void onLocationChanged(Location location) {
        Timber.d(String.format(Locale.ENGLISH, "onLocationChanged(): location: lat = %f, long = %f", location.getLatitude(), location.getLongitude()));
        mCurrentLocation = location;
        mListener.onLocationChange(location);
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public interface OnLocationChangeListener{
        void onLocationChange(Location location);
    }
}
