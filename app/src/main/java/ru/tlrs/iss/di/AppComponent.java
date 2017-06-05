package ru.tlrs.iss.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import javax.inject.Singleton;

import dagger.Component;
import ru.tlrs.iss.Config;
import ru.tlrs.iss.fragments.MapFragment;
import ru.tlrs.iss.fragments.PreferenceListenerFragment;
import ru.tlrs.iss.utils.LocationProvider;

/**
 * Created by thelongrunsmoke.
 */

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
    Context getAppContext();
    SharedPreferences getPreferences();
    Resources getResources();
    void inject(Config config);
    void inject(MapFragment mapFragment);
    void inject(PreferenceListenerFragment locationPreferenceFragment);
    void inject(LocationProvider locationProvider);;
}
