package ru.tlrs.iss.fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import org.osmdroid.views.overlay.OverlayItem;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.tlrs.iss.R;
import ru.tlrs.iss.utils.AssetsManager;
import ru.tlrs.iss.utils.LocationManagerHelper;
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
        mMap.setTileProvider(mapConfig());
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
    private MapTileProviderArray mapConfig() {
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
     * Make permission request.
     */
    private void getLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION_PERMISSION_REQUEST);
        } else {
            locationPermissionGranted();
        }
    }

    private void drawLocation(){
        Timber.d("drawLocation()");
        Location location = LocationManagerHelper.getInstance().getCurrentLocation();
        if (location != null) {
            //your items
            ArrayList<OverlayItem> items = new ArrayList<>();
            items.add(new OverlayItem("","",new GeoPoint(location))); // Lat/Lon decimal degrees

            ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<>(getActivity(), items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                @Override
                public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                    //do something
                    return true;
                }

                @Override
                public boolean onItemLongPress(final int index, final OverlayItem item) {
                    return false;
                }
            });
            mOverlay.setFocusItemsOnTap(true);
            mMap.getOverlays().add(0, mOverlay);
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
     * When permission granted proceed to receiving location.
     */
    private void locationPermissionGranted() {
        drawLocation();
        LocationManagerHelper.getInstance().setLocationChangeListener(new LocationManagerHelper.OnLocationChangeListener() {
            @Override
            public void onLocationChange(Location location) {
                drawLocation();
            }
        });
    }

    /**
     * Show dialog and request permission again.
     */
    private void locationPermissionDenied() {

    }
}
