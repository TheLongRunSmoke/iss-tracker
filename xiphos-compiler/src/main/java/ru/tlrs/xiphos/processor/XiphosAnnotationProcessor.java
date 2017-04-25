package ru.tlrs.xiphos.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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

import ru.tlrs.xiphos.annotations.Field;
import ru.tlrs.xiphos.annotations.Table;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({
        "ru.tlrs.xiphos.annotations.Table",
        "ru.tlrs.xiphos.annotations.Field"
})
public class XiphosAnnotationProcessor extends AbstractProcessor {

    private static final String XIPHOS_PACKAGE = "ru.tlrs.xiphos";

    private static final ClassName SUPER_CREATOR = ClassName.get(XIPHOS_PACKAGE + ".ancestors", "AbstractCreator");
    private static final ClassName SUPER_MIGRATOR = ClassName.get(XIPHOS_PACKAGE + ".ancestors", "AbstractMigrator");

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processTableAnnotations(roundEnv);
        return true;
    }

    private void processTableAnnotations(RoundEnvironment roundEnv) {
        ArrayList<String> tablesInitQueries = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Table.class)) {
            CodeBlock.Builder query = CodeBlock.builder().add("\"CREATE TABLE $L (", getTableName(element));
            query.add("_id INTEGER PRIMARY KEY AUTOINCREMENT, ");
            Iterator<VariableElement> fields = ElementFilter.fieldsIn(element.getEnclosedElements()).iterator();
            while (fields.hasNext()) {
                VariableElement field = fields.next();
                query.add("$L $L", getColumnName(field), getColumnType(field));
                if (fields.hasNext()) query.add(", ");
            }
            query.add(");\"");
            tablesInitQueries.add(query.build().toString());
        }

        CodeBlock.Builder array = CodeBlock.builder().add("new String[]{\n");
        array.add(delimiterJoin(tablesInitQueries.iterator(), ",\n")).add("\n");
        array.add("}");

        FieldSpec init = FieldSpec.builder(String[].class, "QUERIES")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer(array.build())
                .build();

        TypeSpec creator = TypeSpec.classBuilder("Creator")
                .addModifiers(Modifier.PUBLIC)
                .superclass(SUPER_CREATOR)
                .addField(init)
                .build();

        JavaFile javaFile = JavaFile.builder(XIPHOS_PACKAGE + ".generated", creator)
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTableName(Element element) {
        return element.getAnnotation(Table.class).name().isEmpty() ?
                element.getSimpleName().toString().toLowerCase() :
                element.getAnnotation(Table.class).name();
    }

    private String getColumnName(Element element) {
        return element.getAnnotation(Field.class).column().isEmpty() ?
                element.getSimpleName().toString().toLowerCase().substring(1) :
                element.getAnnotation(Field.class).column();
    }

    private String getColumnType(Element element) {
        if (element.getAnnotation(Field.class).type() == Field.Type.NULL) {
            String type = element.asType().toString();
            if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("java.lang.Integer") ||
                    type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("java.lang.Boolean")) {
                return Field.Type.INTEGER.name();
            } else if (type.equalsIgnoreCase("float") || type.equalsIgnoreCase("java.lang.Float") ||
                    type.equalsIgnoreCase("double") || type.equalsIgnoreCase("java.lang.Double")) {
                return Field.Type.REAL.name();
            } else {
                return Field.Type.TEXT.name();
            }
        }
        return element.getAnnotation(Field.class).type().name();
    }

    private String delimiterJoin(Iterator<String> iterator, String delimiter) {
        StringBuilder delimitedString = new StringBuilder("");
        while (iterator.hasNext()) {
            delimitedString.append(iterator.next());
            if (iterator.hasNext()) delimitedString.append(delimiter);
        }
        return delimitedString.toString();
    }
}
