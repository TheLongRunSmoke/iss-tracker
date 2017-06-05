package ru.tlrs.iss.models;

import android.content.ContentValues;

import ru.tlrs.xiphos.Xiphos;
import ru.tlrs.xiphos.annotations.Field;
import ru.tlrs.xiphos.annotations.Table;
import ru.tlrs.xiphos.annotations.Unique;
import ru.tlrs.xiphos.generated.XiphosPass;
import ru.tlrs.xiphos.generated.XiphosTLE;

@Table
public class TLE {

    @Field
    @Unique
    private int mTimestamp;
    @Field
    private String mTLE;

    public TLE(int timestamp, String tle) {
        this.mTimestamp = timestamp;
        this.mTLE = tle;
    }

    public int getTimestamp() {
        return mTimestamp;
    }

    public String getTLE() {
        return mTLE;
    }

    public ContentValues buildContentValues() {
        ContentValues result = new ContentValues();
        result.put(XiphosTLE.Fields.TIMESTAMP.name(), mTimestamp);
        result.put(XiphosTLE.Fields.TLE.name(), mTLE);
        return null;
    }
}
