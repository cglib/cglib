package net.sf.cglib.transform;

import net.sf.cglib.core.Emitter;
import net.sf.cglib.core.Signature;
import net.sf.cglib.core.TypeUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;

abstract public class EmittingTransformer extends ClassTransformer {
    protected Emitter e;
    
    abstract protected Emitter getEmitter(ClassVisitor cv);

    public void setTarget(ClassVisitor cv) {
        super.setTarget(cv);
        e = getEmitter(cv);
    }

    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        e.begin_class(access,
                      name.replace('/', '.'), // TypeUtils.fromInternalName(name).getClassName(),
                      TypeUtils.fromInternalName(superName),
                      TypeUtils.fromInternalNames(interfaces),
                      sourceFile);
    }
    
    public void visitEnd() {
        e.end_class();
    }
    
    public void visitField(int access, String name, String desc, Object value) {
        e.declare_field(access, name, Type.getType(desc), value);
    }

    // TODO: handle visitInnerClass?
    
    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
        return e.begin_method(access,
                              new Signature(name, desc),
                              TypeUtils.fromInternalNames(exceptions));
    }
}
