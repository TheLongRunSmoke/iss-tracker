package ru.tlrs.xiphos.utils;

import java.util.Locale;

public class DevTimeExceptions {

    public static class XiphosCompilerException extends RuntimeException{
        public XiphosCompilerException(String className) {
            super(String.format(Locale.ENGLISH, "%s class not found. Check xiphos-compiler.", className));
        }
    }

    public static class InstantiatingException extends RuntimeException{
        InstantiatingException(String className, String exception) {
            super(String.format(Locale.ENGLISH, "%s for %s class.", exception, className));
        }
    }

}
