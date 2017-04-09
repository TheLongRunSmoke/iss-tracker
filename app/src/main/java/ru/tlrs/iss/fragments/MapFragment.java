package ru.tlrs.iss.fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.TimeUtils;
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
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.tlrs.iss.Config;
import ru.tlrs.iss.R;
import ru.tlrs.iss.dialogs.DialogHelper;
import ru.tlrs.iss.utils.AssetsManager;
import ru.tlrs.iss.utils.LocationProvider;
import timber.log.Timber;


public class MapFragment extends Fragment {

    @BindView(R.id.mapView)
    MapView mMap;

    private static final String ATLAS_FILENAME = "iss.mbtiles";
    private static final int COARSE_LOCATION_PERMISSION_REQUEST = 1;

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
        if (Config.getInstance().isLocationUseEnable()) {
            // If location unknown or needs to be update.
            if (Config.getInstance().isSavedLocationZero() || Config.getInstance().isUpdateLocationOnStartup()) {
                Timber.d("getLocation(): try to update location.");
                if (isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    locationPermissionGranted();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION_PERMISSION_REQUEST);
                }
            } else {
                drawLocation(Config.getInstance().getSavedLocation());
            }
        }
    }

    private void drawLocation(Location location) {
        Timber.d("drawLocation()");
        if (location != null) {
            ArrayList<OverlayItem> items = new ArrayList<>();
            GeoPoint user = new GeoPoint(location);
            items.add(new OverlayItem("", "", user));
            zoomToUserLocation(location);
            ItemizedIconOverlay<OverlayItem> userOverlay = new ItemizedIconOverlay<>(items, getResources().getDrawable(R.drawable.ic_place_black_24dp), null, getActivity());
            List<Overlay> overlays = mMap.getOverlays();
            if (overlays.size() == 0) {
                overlays.add(0, userOverlay);
            }else {
                overlays.set(0, userOverlay);
            }
        }
    }

    private void zoomToUserLocation(Location location) {
        IMapController mapController = mMap.getController();
        mapController.animateTo(new GeoPoint(location));
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

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * When permission granted proceed to receiving location.
     */
    private void locationPermissionGranted() {
        Timber.d("locationPermissionGranted()");
        if (LocationProvider.getInstance().isProviderEnabled()) {
            Location location = LocationProvider.getInstance().getCurrentLocation();
            if (location != null) drawLocation(location);
            LocationProvider.getInstance().setLocationChangeListener(new LocationProvider.OnLocationChangeListener() {
                @Override
                public void onLocationChange(Location location) {
                    drawLocation(location);
                }
            });
            LocationProvider.getInstance().requestUpdate();
        } else {
//            startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
            Timber.d("locationPermissionGranted(): provider disabled");
        }
    }

    /**
     * Show dialog and request permission again.
     */
    private void locationPermissionDenied() {
        DialogHelper.createTwoButtonDialog(getActivity(), );
    }
}
