package net.sf.cglib.transform;

import net.sf.cglib.core.CodeGenerationException;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;

abstract public class AbstractClassTransformer extends ClassAdapter implements ClassTransformer {
    protected AbstractClassTransformer() {
        super(null);
    }

    public void setTarget(ClassVisitor target, ClassVisitor outer) {
        cv = target;
    }
}
