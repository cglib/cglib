package net.sf.cglib.transform;

import org.objectweb.asm.ClassVisitor;

public interface ClassTransformer extends ClassVisitor, Cloneable {
    public void setTarget(ClassVisitor target);
    public Object clone();
}
