package ru.tlrs.xiphos.ancestors;

import java.lang.reflect.Field;

import ru.tlrs.xiphos.utils.DevTimeExceptions.*;
import ru.tlrs.xiphos.utils.GeneratedClassObtainer;

public abstract class AbstractCreator {

    public String[] getQueries(){
        String[] result;
        try {
            Field field = getClass().getDeclaredField("QUERIES");
            result = (String[]) field.get/*from*/(this);
        } catch (NoSuchFieldException e) {
            throw new XiphosCompilerException("Filed QUERIES in Creator do not exist.");
        } catch (IllegalAccessException e) {
            throw new XiphosCompilerException("Inaccessible filed QUERIES in Creator");
        }
        return result;
    }

}
