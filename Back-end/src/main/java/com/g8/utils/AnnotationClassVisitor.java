package com.g8.utils;

import com.g8.model.ClassInfo;
import com.g8.model.FieldInfo;
import com.g8.model.MethodInfo;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnnotationClassVisitor extends ClassVisitor {

    private ClassInfo classInfo;
    private List<MethodInfo> methodInfoList;
    private List<FieldInfo> fieldInfoList;
    Map<String, List<String>> parentClassToNestedClassesMap;

    public AnnotationClassVisitor(Map<String, List<String>> map) {
        super(Opcodes.ASM9);
        fieldInfoList = new ArrayList<>();
        methodInfoList = new ArrayList<>();
        classInfo = new ClassInfo();
        parentClassToNestedClassesMap = map;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    @Override
    public void visitEnd() {

    }

    public String getFieldType(String descriptor) {
        return "";
    }

    public List<FieldInfo> getFieldInfoList() {
        return fieldInfoList;
    }

    public List<MethodInfo> getMethodInfoList() {
        return methodInfoList;
    }
}
