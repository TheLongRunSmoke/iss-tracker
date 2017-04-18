package ru.tlrs.iss.models;

import ru.tlrs.xiphos.annotations.Field;
import ru.tlrs.xiphos.annotations.Table;

@Table
public class TLE {

    @Field
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
}
