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

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new PrintAnnotationVisitor(descriptor);
    }

    @Override
    public void visitEnd() {
    }
}
