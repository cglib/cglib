package net.sf.cglib.transform;

import net.sf.cglib.core.CodeGenerationException;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;

abstract public class AbstractTransformer extends ClassAdapter implements ClassTransformer {
    protected AbstractTransformer() {
        super(null);
    }
    
    public void setTarget(ClassVisitor target) {
        cv = target;
    }

    public Object clone() {
        try {
            AbstractTransformer t = (AbstractTransformer)super.clone();
            if (cv instanceof ClassTransformer) {
                t.cv = (ClassTransformer)((ClassTransformer)cv).clone();
            }
            return t;
        } catch (CloneNotSupportedException e) {
            throw new CodeGenerationException(e); // should be impossible
        }
    }
}
