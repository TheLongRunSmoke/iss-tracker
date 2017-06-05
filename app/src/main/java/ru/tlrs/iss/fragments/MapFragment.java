package ru.tlrs.iss.fragments;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.tlrs.iss.App;
import ru.tlrs.iss.Config;
import ru.tlrs.iss.R;
import ru.tlrs.iss.activities.SettingsActivity;
import ru.tlrs.iss.dialogs.DialogHelper;
import ru.tlrs.iss.utils.AssetsManager;
import ru.tlrs.iss.utils.LocationProvider;
import timber.log.Timber;


public class MapFragment extends Fragment {

    @Inject
    Config config;
    @Inject
    LocationProvider locationProvider;

    @BindView(R.id.mapView)
    MapView mMap;

    private static final String ATLAS_FILENAME = "iss.mbtiles";

    // Request constants
    private static final int COARSE_LOCATION_PERMISSION_REQUEST = 1;
    private static final int PROVIDER_ENABLE_REQUEST = 2;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Configuration.getInstance().setOsmdroidTileCache(new File(AssetsManager.getAssetsPath("cache.db")));    // Set tile cache db path, just to suppress exception.
        // Inflate layout.
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);
        // Inject dependencies.
        App.getComponent().inject(this);
        // Config map.
        mMap.setUseDataConnection(false);
        mMap.setTileProvider(configTileSource());
        mMap.setMinZoomLevel(3);
        mMap.setMaxZoomLevel(4);
        mMap.setMultiTouchControls(true);
        mMap.setScrollableAreaLimitDouble(new BoundingBox(82, -180, -82, 180));    // Let map to loop scroll along longitude, but constrain in latitude.
        IMapController mapController = mMap.getController();
        mapController.setZoom(3);
        mapController.animateTo(new GeoPoint((double) 0, (double) 0));
        getLocation();
        return view;
    }

    /**
     * Config offline tile source.
     *
     * @return MapTileProviderArray.
     */
    private MapTileProviderArray configTileSource() {
        AssetsManager.unpackFromAssets(ATLAS_FILENAME);
        File[] f = new File[]{new File(AssetsManager.getAssetsPath(ATLAS_FILENAME))};
        MapTileProviderArray providerArray = null;
        try {
            providerArray = new OfflineTileProvider(new SimpleRegisterReceiver(getActivity()), f);
        } catch (Exception e) {
            Timber.w("Can't create OfflineTileProvider." + e.getMessage());
        }
        return providerArray;
    }

    /**
     * Check permission and run location routing.
     */
    private void getLocation() {
        if (config.isLocationUseEnable()) {
            // If location unknown or needs to be update.
            if (config.isSavedLocationZero() || config.isUpdateLocationOnStartup()) {
                Timber.d("getLocation(): try to update location.");
                if (isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    locationPermissionGranted();
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION_PERMISSION_REQUEST);
                }
            } else {
                drawUserLocation(config.getSavedLocation());
            }
        }
    }

    /**
     * Create or update map overlay with marker in user location.
     *
     * @param location user location.
     */
    private void drawUserLocation(Location location) {
        Timber.d("drawUserLocation()");
        if (location != null) {
            ArrayList<OverlayItem> items = new ArrayList<>();
            GeoPoint user = new GeoPoint(location);
            items.add(new OverlayItem("", "", user));
            zoomToUserLocation(location);
            ItemizedIconOverlay<OverlayItem> userOverlay = new ItemizedIconOverlay<>(items, getResources().getDrawable(R.drawable.ic_place_black_24dp), null, getActivity());
            List<Overlay> overlays = mMap.getOverlays();
            if (overlays.size() == 0) {
                overlays.add(0, userOverlay);
            } else {
                overlays.set(0, userOverlay);
            }
        }
    }

    /**
     * Animated zoom to specified coordinate.
     *
     * @param location location.
     */
    private void zoomToUserLocation(Location location) {
        IMapController mapController = mMap.getController();
        mapController.animateTo(new GeoPoint(location));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Catch user returned from system settings, after enabling location provider,
        if (requestCode == PROVIDER_ENABLE_REQUEST) {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case COARSE_LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted();
                } else {
                    locationPermissionDenied();
                }
                break;
            }
        }
    }

    /**
     * Check specified permission.
     *
     * @param permission name from Manifest.permission.
     * @return true if permission granted.
     */
    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * When permission granted proceed to receiving location.
     */
    private void locationPermissionGranted() {
        Timber.d("locationPermissionGranted()");
        if (locationProvider.isProviderEnabled()) {
            Location location = locationProvider.getCurrentLocation();
            if (location != null) drawUserLocation(location);
            locationProvider.setLocationChangeListener(new LocationProvider.OnLocationChangeListener() {
                @Override
                public void onLocationChange(Location location) {
                    drawUserLocation(location);
                }
            });
            locationProvider.requestUpdate();
        } else {
            Timber.d("locationPermissionGranted(): provider disabled");
            AlertDialog dialog = DialogHelper.createOKDialog(getActivity(), R.string.dialog_provider_disabled_message, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), PROVIDER_ENABLE_REQUEST);
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    /**
     * Show dialog and request permission again.
     */
    private void locationPermissionDenied() {
        DialogHelper.createTwoButtonDialog(getActivity(), R.string.dialog_perm_denied_message,
                R.string.dialog_perm_denied_positive, R.string.dialog_perm_denied_negative, new DialogInterface.OnClickListener() {
                    // Positive button
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Try to obtain permission, again.
                        getLocation();
                    }
                }, new DialogInterface.OnClickListener() {
                    // Negative button
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send user to location preference.
                        startActivity(new Intent(getActivity(), SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.LocationPreferenceFragment.class.getName()));
                    }
                }).show();
    }
}
