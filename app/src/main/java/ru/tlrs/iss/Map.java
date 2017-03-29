package ru.tlrs.iss;


import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;


public class Map extends Fragment {

    private static final String ASSETS_PATH = "assets";
    private static final String ATLAS_FILENAME = "iss.mbtiles";

    public Map() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unpackFromAssets(ATLAS_FILENAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Configuration.getInstance().setOsmdroidTileCache(new File(getAssetsPath("cache.db")));
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        MapView map = (MapView) view.findViewById(R.id.mapview);
        map.setUseDataConnection(false);
        map.setTileProvider(mapBeginConfig());
        map.setMinZoomLevel(1);
        map.setMaxZoomLevel(4);
        map.setMultiTouchControls(true);
        map.setScrollableAreaLimitDouble(new BoundingBox(82, -180, -82, 180));
        IMapController mapController = map.getController();
        mapController.setZoom(3);
        return view;
    }

    private MapTileProviderArray mapBeginConfig() {
        SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(getActivity());
        File[] f = new File[]{new File(getAssetsPath(ATLAS_FILENAME))};
        MapTileProviderArray providerArray = null;
        try {
            providerArray = new OfflineTileProvider(simpleReceiver, f);
        } catch (Exception e) {
            Timber.w("Can't create OfflineTileProvider." + e.getMessage());
        }
        return providerArray;
    }

    void unpackFromAssets(String fileName) {
        if (!isAssetExtracted(fileName)) {
            AssetManager assetManager = getActivity().getAssets();
            InputStream in;
            OutputStream out;
            File dir = new File(getAssetsPath(fileName));
            if (!dir.exists()) {
                if (!dir.mkdir()) Timber.w("unpackFromAssets(): Can't create directory: " + dir);
            }
            try {
                in = assetManager.open(fileName);
                String newFileName = getAssetsPath(fileName);
                out = new FileOutputStream(newFileName);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
            } catch (Exception e) {
                Timber.d(e.getMessage());
            }
        }
    }

    private String getAppDirectory() {
        String result = "";
        PackageManager m = getActivity().getPackageManager();
        String s = getActivity().getPackageName();
        PackageInfo p;
        try {
            p = m.getPackageInfo(s, 0);
            result = p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.w("Package not found");
        }
        Timber.d("getAppDirectory(): " + result);
        return result;
    }

    private boolean isAssetExtracted(String fileName) {
        File assets = new File(getAssetsPath(fileName));
        return assets.exists();
    }

    private String getAssetsPath(String fileName){
        return getAppDirectory() + "/" + ASSETS_PATH + ((fileName != null) ? ("/" + fileName) : "");
    }
}
