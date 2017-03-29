package ru.tlrs.iss;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.util.MapViewerTemplate;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

public class MapActivity extends MapViewerTemplate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidGraphicFactory.createInstance(this.getApplication());
        unpackFromAssets(getMapFileName());
        super.onCreate(savedInstanceState);
        mapView.setCenter(new LatLong(0, 0));
        mapView.setZoomLevel((byte) 1);
        mapView.setBuiltInZoomControls(true);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_map;
    }

    @Override
    protected int getMapViewId() {
        return R.id.mapView;
    }

    @Override
    protected String getMapFileName() {
        return "world-lowres-0-7.map";
    }

    @Override
    protected XmlRenderTheme getRenderTheme() {
        return InternalRenderTheme.OSMARENDER;
    }

    @Override
    protected void createLayers() {
        MapViewPosition mapViewPosition = mapView.getModel().mapViewPosition;
        TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(tileCaches.get(0),
                mapViewPosition, getMapFile(), getRenderTheme(), false, true, false);
        mapView.getLayerManager().getLayers().add(tileRendererLayer);
    }

    /**
     * Creates the tile cache with the AndroidUtil helper
     */
    @Override
    protected void createTileCaches() {
        tileCaches.add(AndroidUtil.createTileCache(this, getPersistableId(),
                mapView.getModel().displayModel.getTileSize(), this.getScreenRatio(),
                mapView.getModel().frameBufferModel.getOverdrawFactor()));
    }

    /**
     * Obtain app data folder.
     *
     * @return path to data folder or null.
     */
    private String getAppDataDirectory() {
        PackageManager manager = getPackageManager();
        String packageName = getPackageName();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.w("Package name not found.");
        }
        return (info != null) ? info.applicationInfo.dataDir : null;
    }

    @Override
    protected File getMapFileDirectory() {
        String path = getAppDataDirectory();
        return (path != null) ? new File(getAppDataDirectory()) : null;
    }

    private boolean isAssetExtracted(String fileName) {
        String path = getAppDataDirectory();
        File atlas = new File(path + "/" + fileName);
        Log.d("LOG", "isAssetExtracted(): " + atlas.getPath());
        return atlas.exists();
    }

    void unpackFromAssets(String fileName) {
        if (!isAssetExtracted(fileName)) {
            AssetManager assetManager = getAssets();
            InputStream in;
            OutputStream out;
            File dir = new File(getAppDataDirectory());
            if (!dir.exists()) {
                if (!dir.mkdir())
                    Timber.w("unpackFromAssets(): Can't create directory: " + dir);
            }
            try {
                in = assetManager.open(fileName);
                String newFileName = getAppDataDirectory() + "/" + fileName;
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
                Log.e("LOG", "Exception: " + ex.getMessage());
            }
        } else {
            Log.d("LOG", "Map all ready extracted.");
        }
    }

    @Override
    protected void onDestroy() {
        this.mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroy();
    }

    private enum InternalRenderTheme implements XmlRenderTheme {

        DEFAULT("/assets/mapsforge/default.xml"),
        OSMARENDER("/assets/mapsforge/osmarender.xml");

        private final String path;

        InternalRenderTheme(String path) {
            this.path = path;
        }

        @Override
        public XmlRenderThemeMenuCallback getMenuCallback() {
            return null;
        }

        /**
         * @return the prefix for all relative resource paths.
         */
        @Override
        public String getRelativePathPrefix() {
            return "/assets/";
        }

        @Override
        public InputStream getRenderThemeAsStream() {
            return getClass().getResourceAsStream(this.path);
        }
    }
}
