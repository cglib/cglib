package net.sf.cglib.transform;

import org.objectweb.asm.*;

abstract public class AbstractFilterTransformer extends AbstractClassTransformer {
    protected ClassTransformer pass;
    
    protected AbstractFilterTransformer(ClassTransformer pass) {
        this.pass = pass;
    }

    public void setTarget(ClassVisitor target, ClassVisitor outer) {
        super.setTarget(target, outer);
        pass.setTarget(target, outer);
    }
}
