package ru.tlrs.iss;

import android.app.Application;
import android.util.Log;

import dagger.Module;
import ru.tlrs.iss.di.AppComponent;
import ru.tlrs.iss.di.AppModule;
import ru.tlrs.iss.di.DaggerAppComponent;
import ru.tlrs.xiphos.Xiphos;
import timber.log.Timber;

@Module
public class App extends Application {

    private static AppComponent mComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
        Xiphos.init(this.getApplicationContext(), "main.db");
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }

    /**
     * A tree which logs important information for crash reporting.
     */
    private static class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

            /*FakeCrashLibrary.log(priority, tag, message);
            if (t != null) {
                if (priority == Log.ERROR) {
                    FakeCrashLibrary.logError(t);
                } else if (priority == Log.WARN) {
                    FakeCrashLibrary.logWarning(t);
                }
            }*/
        }
    }

    public static AppComponent getComponent() {
        return App.mComponent;
    }
}