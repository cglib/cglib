package net.sf.cglib.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;

class ClassVisitorTee implements ClassVisitor {
    private ClassVisitor t1;
    private ClassVisitor t2;
    
    public ClassVisitorTee(ClassVisitor t1, ClassVisitor t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        t1.visit(access, name, superName, interfaces, sourceFile);
        t2.visit(access, name, superName, interfaces, sourceFile);
    }
    
    public void visitEnd() {
        t1.visitEnd();
        t2.visitEnd();
    }
    
    public void visitField(int access, String name, String desc, Object value) {
        t1.visitField(access, name, desc, value);
        t2.visitField(access, name, desc, value);
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        t1.visitInnerClass(name, outerName, innerName, access);
        t2.visitInnerClass(name, outerName, innerName, access);
    }
    
    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
        t1.visitMethod(access, name, desc, exceptions);
        return t2.visitMethod(access, name, desc, exceptions);
    }
}
  
