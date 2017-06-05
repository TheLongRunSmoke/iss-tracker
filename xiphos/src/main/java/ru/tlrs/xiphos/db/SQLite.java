package ru.tlrs.xiphos.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ru.tlrs.xiphos.utils.GeneratedClassObtainer;

public class SQLite implements DBHelper{

    private static final String LOG_TAG = SQLite.class.getSimpleName();

    private SQLiteHelper mHelper;

    public SQLite(Context context, String dbName) {
        mHelper = new SQLiteHelper(context, dbName, null);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return mHelper.getWritableDatabase();
    }

    public class SQLiteHelper extends SQLiteOpenHelper implements DBOpenHelper{

        public SQLiteHelper(Context context, String name, DatabaseErrorHandler errorHandler) {
            super(context, name, null, 1, errorHandler);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "onCreate()");
            String[] queries = GeneratedClassObtainer.getCreator().getQueries();
            for (String query: queries){
                db.execSQL(query);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Stripped.
        }
    }
}
