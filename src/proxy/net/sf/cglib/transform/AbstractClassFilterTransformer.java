package net.sf.cglib.transform;

import org.objectweb.asm.*;

abstract public class AbstractClassFilterTransformer extends AbstractClassTransformer {
    private ClassTransformer pass;
    private ClassVisitor target;

    public void setTarget(ClassVisitor target) {
        super.setTarget(target);
        pass.setTarget(target);
    }

    protected AbstractClassFilterTransformer(ClassTransformer pass) {
        this.pass = pass;
    }

    abstract protected boolean accept(int access, String name, String superName, String[] interfaces, String sourceFile);

    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        target = accept(access, name, superName, interfaces, sourceFile) ? pass : cv;
        target.visit(access, name, superName, interfaces, sourceFile);
    }

    public void visitEnd() {
        target.visitEnd();
        target = null; // just to be safe
    }

    public void visitField(int access, String name, String desc, Object value, Attribute attrs) {
        target.visitField(access, name, desc, value, attrs);
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        target.visitInnerClass(name, outerName, innerName, access);
    }

    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions, Attribute attrs) {
        return target.visitMethod(access, name, desc, exceptions, attrs);
    }

    public void visitAttribute(Attribute attrs) {
        target.visitAttribute(attrs);
    }
}
