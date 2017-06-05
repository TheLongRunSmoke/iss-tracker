package ru.tlrs.xiphos.processor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import ru.tlrs.xiphos.annotations.Unique;

/**
 * Created by thelongrunsmoke.
 */

public class ElementUtils {

    public static List<VariableElement> findFieldsAnnotatedWith(Element element, Class<? extends Annotation> annotation){
        List<VariableElement> result = new ArrayList<>();
        for (VariableElement field: ElementFilter.fieldsIn(element.getEnclosedElements())){
            if (field.getAnnotation(annotation) != null){
                result.add(field);
            }
        }
        return result;
    }

    public static VariableElement findFieldAnnotatedWith(Element element, Class<? extends Annotation> annotation){
        for (VariableElement field: ElementFilter.fieldsIn(element.getEnclosedElements())){
            if (field.getAnnotation(annotation) != null){
                return field;
            }
        }
        throw new UniqueFieldNotFound(element);
    }

    private static class UniqueFieldNotFound extends RuntimeException {
        UniqueFieldNotFound(Element element) {
            super(String.format(Locale.ENGLISH, "There is no filed annotated with @Unique in %s class.", element.getSimpleName().toString()));
        }
    }
}
