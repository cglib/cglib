package net.sf.cglib.transform;

import org.objectweb.asm.ClassVisitor;

public interface ClassTransformer extends ClassVisitor {
    public void setTarget(ClassVisitor target);
}
