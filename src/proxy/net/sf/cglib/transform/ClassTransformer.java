package net.sf.cglib.transform;

import net.sf.cglib.core.CodeGenerationException;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.CodeVisitor;

// cannot extend from ClassAdapter because cv is final
abstract public class ClassTransformer implements ClassVisitor, Cloneable {
    protected ClassVisitor cv; // TODO: make this private?
    
    protected ClassTransformer() {
    }

    public void wire(ClassVisitor cv) {
        this.cv = cv;
    }

    public Object clone() {
        try {
            ClassTransformer t = (ClassTransformer)super.clone();
            if (cv instanceof ClassTransformer) {
                t.cv = (ClassTransformer)((ClassTransformer)cv).clone();
            }
            return t;
        } catch (CloneNotSupportedException e) {
            throw new CodeGenerationException(e); // should be impossible
        }
    }

    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        cv.visit(access, name, superName, interfaces, sourceFile);
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        cv.visitInnerClass(name, outerName, innerName, access);
    }

    public void visitField(int access, String name, String desc, Object value) {
        cv.visitField(access, name, desc, value);
    }

    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
        return new CodeAdapter(cv.visitMethod(access, name, desc, exceptions));
    }

    public void visitEnd() {
        cv.visitEnd();
    }
}
