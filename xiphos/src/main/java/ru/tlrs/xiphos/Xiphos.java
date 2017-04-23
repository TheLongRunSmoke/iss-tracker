package ru.tlrs.xiphos;

import android.content.Context;

import ru.tlrs.xiphos.utils.GeneratedClassObtainer;

public class Xiphos {

    private static final String LOG_TAG = Xiphos.class.getSimpleName();

    private static volatile Xiphos sInstance;

    private static Xiphos getInstance(Context context) {
        Xiphos localInstance = sInstance;
        if (localInstance == null) {
            synchronized (Xiphos.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new Xiphos(context);
                }
            }
        }
        return localInstance;
    }

    private Xiphos(Context context) {

    }

    public static void init(Context context){
        GeneratedClassObtainer.getCreator();
    }

}
