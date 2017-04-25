package ru.tlrs.xiphos.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ru.tlrs.xiphos.utils.GeneratedClassObtainer;

public class SQLite implements DBHelper{

    private static final String LOG_TAG = SQLite.class.getSimpleName();

    private static volatile SQLite sInstance;

    private SQLite() {
    }

    public class SQLiteHelper extends SQLiteOpenHelper implements DBOpenHelper{

        public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, DatabaseErrorHandler errorHandler) {
            super(context, name, cursorFactory, 1, errorHandler);
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

        }
    }
}
