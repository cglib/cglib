package net.sf.cglib.transform;

import org.objectweb.asm.*;

public class ClassFilterTransformer extends AbstractFilterTransformer {
    private ClassFilter filter;
    private boolean accepted;
    
    public ClassFilterTransformer(ClassFilter filter, ClassTransformer pass) {
        super(pass);
        this.filter = filter;
    }

    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        accepted = filter.accept(name);
        (accepted ? pass : cv).visit(access, name, superName, interfaces, sourceFile);
    }

    public void visitEnd() {
        (accepted ? pass : cv).visitEnd();
    }

    public void visitField(int access, String name, String desc, Object value) {
        (accepted ? pass : cv).visitField(access, name, desc, value);
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        (accepted ? pass : cv).visitInnerClass(name, outerName, innerName, access);
    }

    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
        return (accepted ? pass : cv).visitMethod(access, name, desc, exceptions);
    }
}
