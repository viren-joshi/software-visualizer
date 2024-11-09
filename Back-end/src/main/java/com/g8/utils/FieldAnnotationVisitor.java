package com.g8.utils;

import com.g8.model.FieldInfo;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

public class FieldAnnotationVisitor extends FieldVisitor {

    private FieldInfo fieldInfo;

    public FieldAnnotationVisitor(FieldInfo fieldInfo) {
        super(Opcodes.ASM9);
        this.fieldInfo = fieldInfo;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
       return new PrintAnnotationVisitor(descriptor);
    }

    @Override
    public void visitEnd() {

    }
}
