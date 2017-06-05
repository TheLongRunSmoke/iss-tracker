package ru.tlrs.xiphos.ancestors;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import ru.tlrs.xiphos.Xiphos;

/**
 * Created by thelongrunsmoke.
 */

public abstract class AbstractORM extends BaseAdapter{

    protected volatile SQLiteDatabase mDatabase;

    protected AbstractORM() {
        injectDatabase();
    }

    private void injectDatabase(){
        mDatabase = Xiphos.getWritableDatabase();
    }

    public int getCount(String table){
        Cursor c = mDatabase.rawQuery("SELECT count(*) as count FROM " + table, null);
        int result = 0;
        if (c.moveToFirst()){
            result = c.getInt(c.getColumnIndex("count"));
        }
        c.close();
        return result;
    }

    protected boolean isExist(String table, String field, ContentValues what){
        Cursor c = mDatabase.rawQuery("SELECT 1 FROM "+table+" WHERE " + field + " = ?", new String[]{what.getAsString(field)});
        if (c.moveToFirst()){
            c.close();
            return true;
        }
        c.close();
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    public abstract String getTableName();

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
