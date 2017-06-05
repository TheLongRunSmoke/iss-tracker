package ru.tlrs.xiphos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import ru.tlrs.xiphos.db.DBHelper;
import ru.tlrs.xiphos.db.DBOpenHelper;
import ru.tlrs.xiphos.db.SQLite;
import ru.tlrs.xiphos.utils.GeneratedClassObtainer;

public class Xiphos {

    private static final String LOG_TAG = Xiphos.class.getSimpleName();

    private static volatile DBHelper sHelper;

    private static volatile SQLiteDatabase sDatabase;

    public static void init(Context context, String dbName) {
        initHelper(context, dbName);
        prepareDatabase();
    }

    public static void close(){
        sDatabase.close();
    }

    private static void initHelper(Context context, String dbName) {
        DBHelper localInstance = sHelper;
        if (localInstance == null) {
            synchronized (DBHelper.class) {
                localInstance = sHelper;
                if (localInstance == null) {
                    sHelper = new SQLite(context, dbName);
                }
            }
        }
    }

    private static void prepareDatabase() {
        SQLiteDatabase localInstance = sDatabase;
        if (localInstance == null) {
            synchronized (SQLiteDatabase.class) {
                localInstance = sDatabase;
                if (localInstance == null) {
                    sDatabase = sHelper.getWritableDatabase();
                }
            }
        }
    }

    public static SQLiteDatabase getWritableDatabase(){
        return Xiphos.sDatabase;
    }

}
