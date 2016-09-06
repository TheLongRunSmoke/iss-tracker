package ru.tlrs.iss;


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
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class Map extends Fragment {

    private static final String TAG = Map.class.getSimpleName();

    // Most of this is useless
    private XYTileSource MBTILESRENDER = new XYTileSource(
            "iss",
            0, 10,
            256, ".png", new String[] {});

    private MapTileProviderArray mProvider;

    public Map() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unpackFromAssets("iss.mbtiles");
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
        IMapController mapController = map.getController();
        mapController.setZoom(1);
        return view;
    }

    private MapTileProviderArray mapBeginConfig(){
        SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(getActivity());

        File f = new File(Environment.getExternalStorageDirectory(), "/issabove/iss.mbtiles");

        IArchiveFile[] files = { MBTilesFileArchive.getDatabaseFileArchive(f) };
        MapTileModuleProviderBase moduleProvider = new MapTileFileArchiveProvider(simpleReceiver, MBTILESRENDER, files);

        mProvider = new MapTileProviderArray(MBTILESRENDER, null,
                new MapTileModuleProviderBase[]{ moduleProvider }
        );

        //MapView mOsmv = new MapView(getActivity(), mProvider, null);
        return mProvider;
    }

    void unpackFromAssets(String fileName){
        AssetManager assetManager = getActivity().getAssets();
        InputStream in;
        OutputStream out;
        File dir = new File(Environment.getExternalStorageDirectory() + "/issabove");
        if (!dir.exists()){
            if (!dir.mkdir()) Log.e(TAG, "unpackFromAssets: Can't create directory: " + dir);
        }
        try{
            in = assetManager.open(fileName);
            String newFileName = Environment.getExternalStorageDirectory() + "/issabove/" + fileName;
            out = new FileOutputStream(newFileName);
            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        }catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }

    }

}
