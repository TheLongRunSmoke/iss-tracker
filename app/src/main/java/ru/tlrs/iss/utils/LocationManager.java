package ru.tlrs.iss.utils;

import android.content.Context;
import android.location.LocationListener;
import android.os.Bundle;

import ru.tlrs.iss.App;

/**
 * Created by thelongrunsmoke.
 */

public class LocationManager {

    private static volatile LocationManager sInstance;

    public static LocationManager getInstance() {
        LocationManager localInstance = sInstance;
        if (localInstance == null) {
            synchronized (LocationManager.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new LocationManager();
                }
            }
        }
        return localInstance;
    }

    public LocationManager() {
        // Acquire a reference to the system LocationManager Manager
        android.location.LocationManager locationManager = (android.location.LocationManager) App.getAppContext().getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }
}
