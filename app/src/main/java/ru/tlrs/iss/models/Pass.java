package ru.tlrs.iss.models;

import android.content.ContentValues;

import ru.tlrs.xiphos.annotations.Field;
import ru.tlrs.xiphos.annotations.Table;
import ru.tlrs.xiphos.annotations.Unique;
import ru.tlrs.xiphos.generated.XiphosPass;

/**
 * Created by thelongrunsmoke.
 */

@Table(name = "passes")
public class Pass{

    @Field
    @Unique
    private int mTimestamp;

    private String data;

    public Pass(int mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public ContentValues buildContentValues(){
        ContentValues result = new ContentValues();
        result.put(XiphosPass.Fields.TIMESTAMP.name(), mTimestamp);
        return result;
    }

}
