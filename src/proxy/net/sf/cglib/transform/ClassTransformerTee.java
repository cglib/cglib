package net.sf.cglib.transform;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;

public class ClassTransformerTee extends ClassAdapter implements ClassTransformer {
    private ClassVisitor branch;
    
    public ClassTransformerTee(ClassVisitor branch) {
        super(null);
        this.branch = branch;
    }
    
    public void setTarget(ClassVisitor target, ClassVisitor outer) {
        cv = new ClassVisitorTee(branch, target);
    }
}
