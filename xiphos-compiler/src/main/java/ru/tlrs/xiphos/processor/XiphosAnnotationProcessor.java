package ru.tlrs.xiphos.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import ru.tlrs.xiphos.annotations.Field;
import ru.tlrs.xiphos.annotations.Table;
import ru.tlrs.xiphos.annotations.Unique;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({
        "ru.tlrs.xiphos.annotations.Table",
        "ru.tlrs.xiphos.annotations.Field"
})
public class XiphosAnnotationProcessor extends AbstractProcessor {

    private static final String XIPHOS_PACKAGE = "ru.tlrs.xiphos";

    private static final ClassName SUPER_CREATOR = ClassName.get(XIPHOS_PACKAGE + ".ancestors", "AbstractCreator");
    private static final ClassName SUPER_MIGRATOR = ClassName.get(XIPHOS_PACKAGE + ".ancestors", "AbstractMigrator");  // Stripped
    private static final ClassName SUPER_ORM = ClassName.get(XIPHOS_PACKAGE + ".ancestors", "AbstractORM");

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processTableAnnotations(roundEnv);
        processORM(roundEnv);
        return true;
    }

    private void processTableAnnotations(RoundEnvironment roundEnv) {
        ArrayList<String> tablesInitQueries = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Table.class)) {
            CodeBlock.Builder query = CodeBlock.builder().add("\"CREATE TABLE $L (", getTableName(element));
            query.add("_id INTEGER PRIMARY KEY AUTOINCREMENT, ");
            Iterator<VariableElement> fields = ElementUtils.findFieldsAnnotatedWith(element, Field.class).iterator();
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
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
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
            if (!(e instanceof FilerException)) e.printStackTrace();
        }
    }

    private void processORM(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Table.class)) {

            String name = "Xiphos" + element.getSimpleName();
            ClassName className = ClassName.get(XIPHOS_PACKAGE + ".generated", name);

            TypeSpec.Builder ormClass = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(SUPER_ORM);

            FieldSpec TABLE = FieldSpec.builder(String.class, "TABLE")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\"$L\"", getTableName(element))
                    .build();
            ormClass.addField(TABLE);

            TypeSpec.Builder fieldsEnum = TypeSpec.enumBuilder("Fields")
                    .addModifiers(Modifier.PUBLIC);
            List<VariableElement> fields = ElementUtils.findFieldsAnnotatedWith(element, Field.class);
            for (VariableElement field : fields) {
                fieldsEnum.addEnumConstant(getColumnName(field).toUpperCase());
            }
            fieldsEnum.addField(FieldSpec.builder(String.class, "UNIQUE").
                    addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\"$L\"", getColumnName(ElementUtils.findFieldAnnotatedWith(element, Unique.class)))
                    .build());
            ormClass.addType(fieldsEnum.build());

            FieldSpec sInstance = FieldSpec.builder(className, "sInstance")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.VOLATILE)
                    .build();
            MethodSpec getInstance = MethodSpec.methodBuilder("getInstance")
                    .returns(className)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .addStatement("$T localInstance = sInstance", className)
                    .beginControlFlow("if (localInstance == null)")
                    .beginControlFlow("synchronized ($T.class)", className)
                    .addStatement("localInstance = sInstance")
                    .beginControlFlow("if (localInstance == null)")
                    .addStatement("sInstance = localInstance = new $T()", className)
                    .endControlFlow()
                    .endControlFlow()
                    .endControlFlow()
                    .addStatement("return localInstance")
                    .build();
            ormClass.addField(sInstance);
            ormClass.addMethod(getInstance);

            MethodSpec getTableName = MethodSpec.methodBuilder("getTableName")
                    .returns(String.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return this.TABLE")
                    .build();
            ormClass.addMethod(getTableName);

            MethodSpec getCount = MethodSpec.methodBuilder("getCount")
                    .returns(TypeName.INT)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return getCount(getTableName())")
                    .build();
            ormClass.addMethod(getCount);

            MethodSpec clear = MethodSpec.methodBuilder("clear")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addStatement("getInstance().mDatabase.rawQuery(\"DELETE FROM $L\", new String[]{})", getTableName(element))
                    .addStatement("getInstance().mDatabase.rawQuery(\"VACUUM\", new String[]{})")
                    .build();
            ormClass.addMethod(clear);

            MethodSpec put = MethodSpec.methodBuilder("put")
                    .addParameter(ClassName.get(element.asType()), "object")
                    .returns(TypeName.BOOLEAN)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addStatement("boolean result = false")
                    .addStatement("$T contentValues = object.buildContentValues()", ClassName.get("android.content", "ContentValues"))
                    .beginControlFlow("if (!getInstance().isExist($N, Fields.UNIQUE, contentValues))", TABLE)
                    .addStatement("// TODO: refactor with insertWithOnConflict(). @thelongrunsmoke 05/20/2017")
                    .addStatement("long inserted = getInstance().mDatabase.insert($N, null, contentValues)", TABLE)
                    .addStatement("if (inserted != -1) result = true")
                    .endControlFlow("// TODO: object update code mast be there. @thelongrunsmoke 05/20/2017")
                    .addStatement("return result")
                    .build();
            ormClass.addMethod(put);

            ormClass.addMethod(composeGetByPosition(element));

            ormClass.addMethod(composeFindBy(element));


            JavaFile javaFile = JavaFile.builder(XIPHOS_PACKAGE + ".generated", ormClass.build())
                    .build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private MethodSpec composeGetByPosition(Element element) {
        MethodSpec.Builder getByPosition = MethodSpec.methodBuilder("getByPosition")
                .addParameter(TypeName.INT, "position")
                .addParameter(String.class, "sortedBy")
                .returns(ClassName.get(element.asType()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("$T result = null", ClassName.get(element.asType()))
                .addStatement("String query = \"SELECT * FROM $L ORDER BY \" + sortedBy + \" DESC LIMIT ?,1\"", getTableName(element))
                .addStatement("$T c = getInstance().mDatabase.rawQuery(query, new String[]{Integer.toString(position)})", ClassName.get("android.database", "Cursor"))
                .beginControlFlow("if(c.moveToFirst())");
        StringBuilder constructorCall = new StringBuilder();
        constructorCall.append("result = new " + element.getSimpleName() + "(");
        Iterator<VariableElement> fields = ElementUtils.findFieldsAnnotatedWith(element, Field.class).iterator();
        while (fields.hasNext()) {
            VariableElement field = fields.next();
            String type = field.asType().toString();
            if (Objects.equals(getColumnType(field), Field.Type.TEXT.name())) {
                constructorCall.append("c.getString(c.getColumnIndex(\"" + getColumnName(field) + "\"))");
            } else if (Objects.equals(getColumnType(field), Field.Type.INTEGER.name())) {
                if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("java.lang.Integer")) {
                    constructorCall.append("c.getInt(c.getColumnIndex(\"" + getColumnName(field) + "\"))");
                } else if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("java.lang.Boolean")) {
                    constructorCall.append("(c.getInt(c.getColumnIndex(\"" + getColumnName(field) + "\")) == 0) ? false : true");
                }
            } else if (Objects.equals(getColumnType(field), Field.Type.REAL.name())) {
                if (type.equalsIgnoreCase("float") || type.equalsIgnoreCase("java.lang.Float")) {
                    constructorCall.append("c.getFloat(c.getColumnIndex(\"" + getColumnName(field) + "\"))");
                } else if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("java.lang.Boolean")) {
                    constructorCall.append("c.getDouble(c.getColumnIndex(\"" + getColumnName(field) + "\"))");
                }
            }
            if (fields.hasNext()) constructorCall.append(", ");
        }
        constructorCall.append(")");
        getByPosition
                .addStatement(constructorCall.toString())
                .endControlFlow()
                .addStatement("c.close()")
                .addStatement("return result");
        return getByPosition.build();
    }

    private MethodSpec composeFindBy(Element element) {
        MethodSpec.Builder getByPosition = MethodSpec.methodBuilder("findBy")
                .addParameter(String.class, "what")
                .addParameter(String.class, "inField")
                .returns(ClassName.get(element.asType()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("$T result = null", ClassName.get(element.asType()))
                .addStatement("String query = \"SELECT * FROM $L WHERE \" + inField + \" = ? LIMIT 0,1\"", getTableName(element))
                .addStatement("$T c = getInstance().mDatabase.rawQuery(query, new String[]{what})", ClassName.get("android.database", "Cursor"))
                .beginControlFlow("if(c.moveToFirst())");
        StringBuilder constructorCall = new StringBuilder();
        constructorCall.append("result = new " + element.getSimpleName() + "(");
        Iterator<VariableElement> fields = ElementUtils.findFieldsAnnotatedWith(element, Field.class).iterator();
        while (fields.hasNext()) {
            VariableElement field = fields.next();
            String type = field.asType().toString();
            if (Objects.equals(getColumnType(field), Field.Type.TEXT.name())) {
                constructorCall.append("c.getString(c.getColumnIndex(\"" + getColumnName(field) + "\"))");
            } else if (Objects.equals(getColumnType(field), Field.Type.INTEGER.name())) {
                if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("java.lang.Integer")) {
                    constructorCall.append("c.getInt(c.getColumnIndex(\"" + getColumnName(field) + "\"))");
                } else if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("java.lang.Boolean")) {
                    constructorCall.append("(c.getInt(c.getColumnIndex(\"" + getColumnName(field) + "\")) == 0) ? false : true");
                }
            } else if (Objects.equals(getColumnType(field), Field.Type.REAL.name())) {
                if (type.equalsIgnoreCase("float") || type.equalsIgnoreCase("java.lang.Float")) {
                    constructorCall.append("c.getFloat(c.getColumnIndex(\"" + getColumnName(field) + "\"))");
                } else if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("java.lang.Boolean")) {
                    constructorCall.append("c.getDouble(c.getColumnIndex(\"" + getColumnName(field) + "\"))");
                }
            }
            if (fields.hasNext()) constructorCall.append(", ");
        }
        constructorCall.append(")");
        getByPosition
                .addStatement(constructorCall.toString())
                .endControlFlow()
                .addStatement("c.close()")
                .addStatement("return result");
        return getByPosition.build();
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
