package net.sf.cglib.transform;

import net.sf.cglib.core.CodeGenerationException;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;

abstract public class ClassTransformer extends ClassAdapter implements Cloneable {
    protected ClassTransformer() {
        super(null);
    }
    
    public void setTarget(ClassVisitor target) {
        cv = target;
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
}
