package net.sf.cglib.transform;

import org.objectweb.asm.*;

public class ClassFilterTransformer extends AbstractFilterTransformer {
    private ClassFilter filter;
    private ClassVisitor target;
    
    public ClassFilterTransformer(ClassFilter filter, ClassTransformer pass) {
        super(pass);
        this.filter = filter;
    }

    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        target = filter.accept(name) ? pass : cv;
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
