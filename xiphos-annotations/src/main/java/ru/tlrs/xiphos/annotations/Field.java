package ru.tlrs.xiphos.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface Field {
    String column() default "";
    Type type() default Type.NULL;

    enum Type{
        NULL,
        TEXT,
        INTEGER,
        REAL,
        OBJECT
    }
}
