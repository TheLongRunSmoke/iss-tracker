package ru.tlrs.iss.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.tlrs.iss.App;
import ru.tlrs.iss.Config;
import ru.tlrs.iss.utils.LocationProvider;

/**
 * Created by thelongrunsmoke.
 */

@Module
public class AppModule {
    private Context mAppContext;
    public AppModule(@NonNull Context context){
        mAppContext = context;
    }

    @Provides
    @Singleton
    Context provideApplicationContext(){
        return mAppContext;
    }

    @Provides
    @Singleton
    Config provideConfig() {
        return new Config();
    }

    @Provides
    @Singleton
    LocationProvider provideLocationProvider() {
        return new LocationProvider(mAppContext);
    }

    @Provides
    @Singleton
    SharedPreferences providePreferences(){
        return PreferenceManager.getDefaultSharedPreferences(mAppContext);
    }

    @Provides
    @Singleton
    Resources provideResources(){
        return mAppContext.getResources();
    }
}
