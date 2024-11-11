package com.g8.utils;

import com.g8.model.MethodInfo;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class MethodAnnotationVisitor extends MethodVisitor {

    private MethodInfo currentMethod;
    private final List<PrintAnnotationVisitor> annotations = new ArrayList<>();


    public MethodAnnotationVisitor(MethodInfo currentMethod) {
        super(Opcodes.ASM9);
        this.currentMethod = currentMethod;
    }

    // Formatting the annotation and setting the variables
    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        String annotationName = descriptor.replace('/', '.').replace(";", "");
        annotationName = "@" + annotationName.substring(1);
        PrintAnnotationVisitor annotationVisitor = new PrintAnnotationVisitor(annotationName);
        annotations.add(annotationVisitor);
        return annotationVisitor;
    }

    // Required because if directly stored in the class list then the values of annotations won't be stored
    @Override
    public void visitEnd() {
        for (PrintAnnotationVisitor annotationVisitor : annotations) {
            String fullAnnotation = annotationVisitor.getFullAnnotation();
            currentMethod.getAnnotations().add(fullAnnotation);
        }
        // Reset list for the next visit
        annotations.clear();
    }
}
