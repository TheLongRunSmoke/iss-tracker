package ru.tlrs.iss;


import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;


public class Map extends Fragment {

    private static final String LOG_TAG = Map.class.getSimpleName();

    private static final String ATLAS_PATH = "asset";
    private static final String ATLAS_FILENAME = "iss.mbtiles";

    private XYTileSource MBTILESRENDER = new XYTileSource("iss", 3, 4, 256, ".png", new String[]{});

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
        MapView map = (MapView) view.findViewById(R.id.mapview);
        map.setUseDataConnection(false);
        MapTileProviderArray provider = mapBeginConfig();
        map.setTileProvider(provider);
        map.setTileSource(MBTILESRENDER);
        map.setMinZoomLevel(3);
        map.setMaxZoomLevel(4);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(1);
        //Timber.d(map.getBoundingBox().toString());
        //BoundingBox box = new BoundingBox(85, 181, -85, -181);
        //map.setScrollableAreaLimitDouble(box);
        //map.zoomToBoundingBox(box, true);
        return view;
    }

    private MapTileProviderArray mapBeginConfig() {

        SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(getActivity());

        File f = new File(getAppDataDirectory() + "/" + ATLAS_PATH + "/" + ATLAS_FILENAME);

        IArchiveFile[] files = {MBTilesFileArchive.getDatabaseFileArchive(f)};
        MapTileModuleProviderBase moduleProvider = new MapTileFileArchiveProvider(simpleReceiver, MBTILESRENDER, files);

        return new MapTileProviderArray(MBTILESRENDER, null,
                new MapTileModuleProviderBase[]{moduleProvider}
        );
    }

    void unpackFromAssets(String fileName) {
        if (!isAssetExtracted(fileName)) {
            AssetManager assetManager = getActivity().getAssets();
            InputStream in;
            OutputStream out;
            File dir = new File(getAppDataDirectory() + "/" + ATLAS_PATH);
            if (!dir.exists()) {
                if (!dir.mkdir())
                    Timber.w("unpackFromAssets(): Can't create directory: " + dir);
            }
            try {
                in = assetManager.open(fileName);
                String newFileName = getAppDataDirectory() + "/" + ATLAS_PATH + "/" + fileName;
                out = new FileOutputStream(newFileName);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
            } catch (Exception ex) {
                Log.e(LOG_TAG, ex.getMessage());
            }
        }
    }

    private boolean isAssetExtracted(String fileName) {
        String path = getAppDataDirectory();
        File atlas = new File(path + "/" + ATLAS_PATH + "/" + fileName);
        return atlas.exists();
    }

    /**
     * Obtain app data folder.
     *
     * @return path to data folder or null.
     */
    private String getAppDataDirectory() {
        PackageManager manager = getActivity().getPackageManager();
        String packageName = getActivity().getPackageName();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.w("Package name not found.");
        }
        return (info != null) ? info.applicationInfo.dataDir : null;
    }
}
