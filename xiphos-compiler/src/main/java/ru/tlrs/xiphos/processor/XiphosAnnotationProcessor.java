package ru.tlrs.xiphos.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import ru.tlrs.xiphos.annotations.Field;
import ru.tlrs.xiphos.annotations.Table;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({
        "ru.tlrs.xiphos.annotations.Table",
        "ru.tlrs.xiphos.annotations.Field"
})
public class XiphosAnnotationProcessor extends AbstractProcessor{

    private static final ClassName creator = ClassName.get("xiphos.ancestors", "Creator");
    private static final ClassName migrator = ClassName.get("xiphos.ancestors", "Migrator");

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        // for each javax.lang.model.element.Element annotated with the CustomAnnotation
        for (Element element : roundEnv.getElementsAnnotatedWith(Table.class)) {
            String objectType = element.getSimpleName().toString();

        }
        return true;
    }
}
