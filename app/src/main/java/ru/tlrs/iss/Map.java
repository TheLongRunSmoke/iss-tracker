package ru.tlrs.iss;


import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
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
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class Map extends Fragment {

    private static final String TAG = Map.class.getSimpleName();

    private static final String ATLAS_PATH = "asset";
    private static final String ATLAS_FILENAME = "iss.mbtiles";

    private XYTileSource MBTILESRENDER = new XYTileSource("iss", 1, 4, 256, ".png", new String[]{});

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
        map.setMinZoomLevel(1);
        map.setMaxZoomLevel(4);
        map.setMultiTouchControls(true);
        map.setScrollableAreaLimit(new BoundingBoxE6(90.0, 180.0, 90.0, 180.0));
        //BoundingBoxE6 box = new BoundingBoxE6(map.getBoundingBox().getLatNorthE6());
        //map.setScrollableAreaLimit(box);
        IMapController mapController = map.getController();
        mapController.setZoom(1);
        return view;
    }

    private MapTileProviderArray mapBeginConfig() {
        SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(getActivity());

        File f = new File(Environment.getExternalStorageDirectory(), "/" + ATLAS_PATH + "/" + ATLAS_FILENAME);

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
            File dir = new File(Environment.getExternalStorageDirectory() + "/" + ATLAS_PATH);
            if (!dir.exists()) {
                if (!dir.mkdir()) Log.e(TAG, "unpackFromAssets: Can't create directory: " + dir);
            }
            try {
                in = assetManager.open(fileName);
                String newFileName = Environment.getExternalStorageDirectory() + "/" + ATLAS_PATH + "/" + fileName;
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
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    private String getAppDirectory() {
        PackageManager m = getActivity().getPackageManager();
        String s = getActivity().getPackageName();
        PackageInfo p;
        try {
            p = m.getPackageInfo(s, 0);
            return p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean isAssetExtracted(String fileName) {
        String path = getAppDirectory();
        File atlas = new File(path + "/" + ATLAS_PATH + "/" + fileName);
        return atlas.exists();
    }
}
