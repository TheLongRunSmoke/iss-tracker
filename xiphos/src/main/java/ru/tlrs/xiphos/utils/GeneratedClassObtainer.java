package ru.tlrs.xiphos.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import ru.tlrs.xiphos.ancestors.AbstractCreator;

public class GeneratedClassObtainer {

    private static final String LOG_TAG = GeneratedClassObtainer.class.getSimpleName();

    public static AbstractCreator getCreator(){
        Class<?> clazz = findClass("ru.tlrs.xiphos.generated.Creator");
        return (AbstractCreator) instantiateClass(clazz);
    }

    private static Class<?> findClass(String name){
        Class<?> clazz;
        try {
            clazz = Class.forName(name);
        }catch (ClassNotFoundException e) {
            throw new XiphosCompilerException(name);
        }
        return clazz;
    }

    private static Object instantiateClass(Class<?> clazz){
        Object instance;
        try {
            Constructor<?> constructor = clazz.getConstructor();
            instance = constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new InstantiatingException(clazz.getSimpleName(), e.getClass().getSimpleName());
        } catch (IllegalAccessException e) {
            throw new InstantiatingException(clazz.getSimpleName(), e.getClass().getSimpleName());
        } catch (InstantiationException e) {
            throw new InstantiatingException(clazz.getSimpleName(), e.getClass().getSimpleName());
        } catch (InvocationTargetException e) {
            throw new InstantiatingException(clazz.getSimpleName(), e.getClass().getSimpleName());
        }
        return instance;
    }

    /*
     * Dev-time Exceptions
     */
    private static class XiphosCompilerException extends RuntimeException{
        XiphosCompilerException(String className) {
            super(String.format(Locale.ENGLISH, "%s class not found. Check xiphos-compiler.", className));
        }
    }

    private static class InstantiatingException extends RuntimeException{
        InstantiatingException(String className, String exception) {
            super(String.format(Locale.ENGLISH, "%s for %s class.", exception, className));
        }
    }
}
