package net.sf.cglib.transform;

import net.sf.cglib.core.Emitter2;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;

abstract public class EmittingTransformer extends ClassTransformer {
    protected Emitter2 e;
    
    abstract protected Emitter2 getEmitter(ClassVisitor cv);

    public void setTarget(ClassVisitor cv) {
        super.setTarget(cv);
        e = getEmitter(cv);
    }

    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        e.begin_class(access, name, superName, interfaces, sourceFile);
    }
    
    public void visitEnd() {
        e.end_class();
    }
    
    public void visitField(int access, String name, String desc, Object value) {
        e.declare_field(access, name, desc, value);
    }

    // TODO: handle visitInnerClass?
    
    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
        e.begin_method(access, name, desc, exceptions);
        return e.getCodeVisitor();
    }
}
