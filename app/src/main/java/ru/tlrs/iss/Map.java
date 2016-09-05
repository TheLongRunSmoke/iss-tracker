package ru.tlrs.iss;


import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
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


public class Map extends Fragment {

    // Most of this is useless
    private XYTileSource MBTILESRENDER = new XYTileSource(
            "iss",
            0, 10,
            256, ".png", new String[] {});

    private MapView mOsmv;
    private MapTileProviderArray mProvider;

    public Map() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
        MapView map = (MapView) view.findViewById(R.id.mapview);
        map.setUseDataConnection(false);
        mapBeginConfig();
        IMapController mapController = map.getController();
        mapController.setZoom(1);
        return view;
    }

    private void mapBeginConfig(){
        SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(this);

        File f = new File(, "map.mbtiles");

        IArchiveFile[] files = { MBTilesFileArchive.getDatabaseFileArchive(f) };
        MapTileModuleProviderBase moduleProvider = new MapTileFileArchiveProvider(simpleReceiver, MBTILESRENDER, files);

        mProvider = new MapTileProviderArray(MBTILESRENDER, null,
                new MapTileModuleProviderBase[]{ moduleProvider }
        );

        this.mOsmv = new MapView(this, 256, mResourceProxy, mProvider);

    }

}
