package ru.tlrs.xiphos.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by thelongrunsmoke.
 */

public interface DBHelper {
    SQLiteDatabase getWritableDatabase();
}
