package ru.tlrs.xiphos;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ru.tlrs.xiphos.ancestors.AbstractCreator;

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
        Class<?> clazz = null;
        AbstractCreator creator = null;
        try {
            clazz = Class.forName("ru.tlrs.xiphos.generated.Creator");
            Constructor<?> constructor = clazz.getConstructor(Context.class);
            creator = (AbstractCreator) constructor.newInstance(context);
        } catch (ClassNotFoundException e) {
            Log.w(LOG_TAG, "ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.w(LOG_TAG, "NoSuchMethodException");
        } catch (InstantiationException e) {
            Log.w(LOG_TAG, "InstantiationException");
        } catch (IllegalAccessException e) {
            Log.w(LOG_TAG, "IllegalAccessException");
        } catch (InvocationTargetException e) {
            Log.w(LOG_TAG, "InvocationTargetException");
        }
        if(creator != null){
            
        }

    }

}
