package net.sf.cglib.transform;

import org.objectweb.asm.*;

abstract public class AbstractFilterTransformer extends AbstractTransformer {
    protected ClassTransformer pass;
    
    protected AbstractFilterTransformer(ClassTransformer pass) {
        this.pass = pass;
    }

    public Object clone() {
        AbstractFilterTransformer t = (AbstractFilterTransformer)super.clone();
        t.pass = (ClassTransformer)pass.clone();
        return t;
    }

    public void setTarget(ClassVisitor target, ClassVisitor outer) {
        super.setTarget(target, outer);
        pass.setTarget(target, outer);
    }
}
