package com.g8.utils;

import com.g8.model.FieldInfo;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class FieldAnnotationVisitor extends FieldVisitor {

    private FieldInfo fieldInfo;
    private final List<PrintAnnotationVisitor> annotations = new ArrayList<>();


    public FieldAnnotationVisitor(FieldInfo fieldInfo) {
        super(Opcodes.ASM9);
        this.fieldInfo = fieldInfo;
    }

    // Formatting the annotation and setting the variables
    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        String annotationName = "@" + descriptor.substring(1).replace("/",".").replace(";","");
        PrintAnnotationVisitor annotationVisitor = new PrintAnnotationVisitor(annotationName);
        fieldInfo.setAnnotated(true);
        annotations.add(annotationVisitor);
        return annotationVisitor;
    }

    // Required because if directly stored in the class list then the values of annotations won't be stored
    @Override
    public void visitEnd() {

        for (PrintAnnotationVisitor annotationVisitor : annotations) {
            String fullAnnotation = annotationVisitor.getFullAnnotation();
            fieldInfo.getAnnotations().add(fullAnnotation);
        }

        // Reset list for the next visit
        annotations.clear();
    }
}
