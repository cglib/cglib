package net.sf.cglib.transform;

import net.sf.cglib.core.CodeGenerationException;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;

abstract public class AbstractTransformer extends ClassAdapter implements ClassTransformer {
    protected AbstractTransformer() {
        super(null);
    }
    
    public void setTarget(ClassVisitor target, ClassVisitor outer) {
        cv = target;
    }
}
