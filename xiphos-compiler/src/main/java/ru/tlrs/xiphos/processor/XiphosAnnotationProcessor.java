package ru.tlrs.xiphos.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.xml.internal.ws.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;

import ru.tlrs.xiphos.annotations.Field;
import ru.tlrs.xiphos.annotations.Table;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({
        "ru.tlrs.xiphos.annotations.Table",
        "ru.tlrs.xiphos.annotations.Field"
})
public class XiphosAnnotationProcessor extends AbstractProcessor {

    private static final String BASE_PACKAGE = "ru.tlrs.xiphos";

    private static final ClassName SUPER_CREATOR = ClassName.get(BASE_PACKAGE + ".ancestors", "AbstractCreator");
    private static final ClassName SUPER_MIGRATOR = ClassName.get(BASE_PACKAGE + ".ancestors", "AbstractMigrator");

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        processTableAnnotations(annotations, roundEnv);

        return true;
    }

    private void processTableAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ArrayList<String> tablesInitQueries = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Table.class)) {
            CodeBlock.Builder query = CodeBlock.builder().add("CREATE TABLE $S (", element.getSimpleName().toString().toLowerCase());
            query.add("_id integer primary key autoincrement, ");
            Iterator<VariableElement> fields = ElementFilter.fieldsIn(element.getEnclosedElements()).iterator();
            while(fields.hasNext()){
                VariableElement field = fields.next();
                query.add("$S text", field.getAnnotation(Field.class).column());
                if (fields.hasNext()) query.add(", ");
            }
            query.add(");");
            tablesInitQueries.add(query.build().toString());
        }

        CodeBlock.Builder array = CodeBlock.builder().add("new String[]{");
        array.add();
        array.addStatement("}");

        FieldSpec init = FieldSpec.builder(String[].class, "TABLE_INIT")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer(array.build())
                .build();

        TypeSpec creator = TypeSpec.classBuilder("Creator")
                .addModifiers(Modifier.PUBLIC)
                .superclass(SUPER_CREATOR)
                .addField(init)
                .build();

        JavaFile javaFile = JavaFile.builder(BASE_PACKAGE + ".generated.Creator", creator)
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
