package ru.tlrs.iss.models;

import ru.tlrs.xiphos.annotations.Field;
import ru.tlrs.xiphos.annotations.Table;

/**
 * Created by thelongrunsmoke.
 */

@Table(name = "passes")
public class Pass {
    @Field
    private int mTimestamp;
}
