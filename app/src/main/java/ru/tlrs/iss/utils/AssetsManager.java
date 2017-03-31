package ru.tlrs.iss.utils;


import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import ru.tlrs.iss.App;
import timber.log.Timber;

public class AssetsManager {

    private static final String ASSETS_FOLDER = "assets";

    /**
     * Extract specified asset to application data folder.
     *
     * @param fileName assets file name.
     */
    public static void unpackFromAssets(String fileName) {
        if (!AssetsManager.isAssetExtracted(fileName)) {
            Timber.d("unpackFromAssets(): begin asset extraction.");
            AssetManager assetManager = App.getAppContext().getAssets();
            InputStream in;
            OutputStream out;
            File dir = new File(AssetsManager.getAssetsPath(null));
            if (!dir.exists()) {
                if (!dir.mkdir()) Timber.e("unpackFromAssets(): Can't create directory: " + dir);
            }
            // Use streams to extract file. 1kB chank enough in most cases.
            try {
                in = assetManager.open(fileName);
                String newFileName = AssetsManager.getAssetsPath(fileName);
                out = new FileOutputStream(newFileName);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
                Timber.d("unpackFromAssets(): Asset extraction complete.");
            } catch (Exception e) {
                Timber.d("unpackFromAssets(): Exception: " + e.getMessage());
            }
        }
    }

    /**
     * Obtain path to app data folder on internal storage.
     *
     * @return full path.
     */
    private static String getAppDirectory() {
        String result = "";
        PackageManager m = App.getAppContext().getPackageManager();
        String s = App.getAppContext().getPackageName();
        PackageInfo p;
        try {
            p = m.getPackageInfo(s, 0);
            result = p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.w("Package not found");
        }
        return result;
    }

    /**
     * Check asset file existent in app data folder.
     *
     * @param fileName asset file name.
     * @return true if exist.
     */
    private static boolean isAssetExtracted(String fileName) {
        File assets = new File(AssetsManager.getAssetsPath(fileName));
        boolean result = assets.exists();
        Timber.d("isAssetExtracted(): " + fileName + (result ? " exist." : " not exist."));
        return result;
    }

    /**
     * Return path to asset file in app data folder. Use null for folder path.
     *
     * @param fileName asset file name.
     * @return full path in app directory.
     */
    public static String getAssetsPath(@Nullable String fileName) {
        return AssetsManager.getAppDirectory() + "/" + ASSETS_FOLDER + ((fileName != null) ? ("/" + fileName) : "");
    }

}
