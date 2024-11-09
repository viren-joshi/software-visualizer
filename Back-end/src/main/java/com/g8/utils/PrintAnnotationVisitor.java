package com.g8.utils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class PrintAnnotationVisitor extends AnnotationVisitor {

    private final String annotationName;
    private final List<String> parameters = new ArrayList<>();

    public PrintAnnotationVisitor(String annotationName) {
        super(Opcodes.ASM9);
        this.annotationName = annotationName;
    }

    @Override
    public void visit(String name, Object value) {
        // Store key-value pairs in the format "name = value"
        if(name == null) {
            parameters.add(value.toString());
        } else {
            parameters.add(String.format("%s = %s", name, value));
        }
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        // Extract and format enum values as "name = ENUM_VALUE"
        if(name == null) {
            parameters.add(value);
        } else {
            descriptor = descriptor.substring(descriptor.lastIndexOf('/') + 1, descriptor.length() - 1);
            String enumValue = descriptor + "." + value.substring(value.lastIndexOf('/') + 1);
            parameters.add(String.format("%s = %s", name, enumValue));
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        // Handle nested annotations as "name = @NestedAnnotation"
        parameters.add(String.format("%s = @%s", name, descriptor.replace('/', '.')));
        return this;
    }

    public String getFullAnnotation() {
        // Return formatted annotation string
        return parameters.isEmpty()
                ? annotationName
                : String.format("%s(%s)", annotationName, String.join(", ", parameters));
    }
}
