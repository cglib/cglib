package net.sf.cglib.transform;

import net.sf.cglib.core.Emitter;
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

    private static Type fromInternalName(String name) {
        // TODO; primitives?
        return Type.getType("L" + name + ";");
    }

    private static Type[] fromInternalNames(String[] names) {
        if (names == null) {
            return null;
        }
        Type[] types = new Type[names.length];
        for (int i = 0; i < names.length; i++) {
            types[i] = fromInternalName(names[i]);
        }
        return types;
    }

    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        e.begin_class(access,
                      fromInternalName(name),
                      fromInternalName(superName),
                      fromInternalNames(interfaces),
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
        e.begin_method(access,
                       name,
                       Type.getReturnType(desc),
                       Type.getArgumentTypes(desc),
                       fromInternalNames(exceptions));
        return e.getCodeVisitor();
    }
}
