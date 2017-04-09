package ru.tlrs.iss.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import java.util.Locale;
import java.util.Random;

import ru.tlrs.iss.App;
import ru.tlrs.iss.BuildConfig;
import ru.tlrs.iss.Config;
import timber.log.Timber;

public final class LocationProvider {

    private static volatile LocationProvider sInstance;

    private static final String LOCATION_MANAGER = android.location.LocationManager.NETWORK_PROVIDER;
    private final LocationManager mLocationManager;
    private Location mCurrentLocation;
    private LocationListener mListener;
    private OnLocationChangeListener mCallback;

    public static LocationProvider getInstance() {
        LocationProvider localInstance = sInstance;
        if (localInstance == null) {
            synchronized (LocationProvider.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new LocationProvider();
                }
            }
        }
        return localInstance;
    }

    private LocationProvider() {
        mLocationManager = (LocationManager) App.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        mListener = new NetworkLocationListener();
        if (isPermissionGranted())
            setCurrentLocation(mLocationManager.getLastKnownLocation(LOCATION_MANAGER));
    }

    public void requestUpdate() {
        if (isPermissionGranted()) {
            Timber.d("requestUpdate()");
            mLocationManager.requestLocationUpdates(LOCATION_MANAGER, 0, 0, mListener);
            if (BuildConfig.DEBUG) mockLocation();
        }
    }

    public boolean isProviderEnabled() {
        Timber.d("isProviderEnabled(): " + mLocationManager.isProviderEnabled(LOCATION_MANAGER));
        return mLocationManager.isProviderEnabled(LOCATION_MANAGER);
    }

    public Location getCurrentLocation() {
        Location result = (mCurrentLocation == null) ? Config.getInstance().getSavedLocation() : mCurrentLocation;
        if (result == null) {
            Timber.d("getCurrentLocation(): null");
            return null;
        }
        Timber.d(String.format(Locale.ENGLISH, "getCurrentLocation(): location: lat = %f, long = %f", result.getLatitude(), result.getLongitude()));
        return result;
    }

    private void setCurrentLocation(Location location) {
        if (location == null) {
            Timber.d("setCurrentLocation(): null");
            return;
        }
        Timber.d(String.format(Locale.ENGLISH, "setCurrentLocation(): lat = %f, long = %f", location.getLatitude(), location.getLongitude()));
        mCurrentLocation = location;
        Config.getInstance().setSavedLocation(location);
    }

    public void setLocationChangeListener(OnLocationChangeListener listener) {
        this.mCallback = listener;
    }

    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(App.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void mockLocation() {
        Timber.d("mockLocation()");
        Random random = new Random();
        try {
            mLocationManager.addTestProvider(LOCATION_MANAGER, false, false, true, false, false, false, false, 0, 10);
            Location mockLocation = new Location(LOCATION_MANAGER);
            mockLocation.setLatitude(random.nextBoolean() ? 82 * random.nextDouble() : -82 * random.nextDouble());
            mockLocation.setLongitude(random.nextBoolean() ? 180 * random.nextDouble() : -180 * random.nextDouble());
            mockLocation.setAccuracy(8);
            mockLocation.setTime(System.currentTimeMillis() / 1000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mockLocation.setElapsedRealtimeNanos(1000000);
            }
            mLocationManager.setTestProviderLocation(LOCATION_MANAGER, mockLocation);
        } catch (SecurityException e) {
            throw new MockNotAllowedException();
        }
    }

    /*
     * Interfaces
     */

    public interface OnLocationChangeListener {
        void onLocationChange(Location location);
    }

    /*
     * Dev-time exceptions.
     */

    private class MockNotAllowedException extends RuntimeException {
        public MockNotAllowedException() {
            super("Allow app to mock location in developer settings.");
        }
    }

    /**
     * Catch location update.
     */
    private class NetworkLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Timber.d(String.format(Locale.ENGLISH, "onLocationChanged(): location: lat = %f, long = %f", location.getLatitude(), location.getLongitude()));
            setCurrentLocation(location);
            mCallback.onLocationChange(location);
            mLocationManager.removeUpdates(this);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Timber.d("onStatusChanged(): provider = " + provider + " status = " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Timber.d("onProviderEnabled(): provider = " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Timber.d("onProviderDisabled(): provider = " + provider);
        }
    }
}
