package net.sf.cglib.transform;

import org.objectweb.asm.*;

public class MethodFilterTransformer extends AbstractClassTransformer {
    private MethodFilter filter;
    private ClassTransformer pass;
    private ClassVisitor direct;
    
    public MethodFilterTransformer(MethodFilter filter, ClassTransformer pass) {
        this.filter = filter;
        this.pass = pass;
        super.setTarget(pass);
    }

    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions, Attribute attrs) {
        return (filter.accept(access, name, desc, exceptions, attrs) ? pass : direct).visitMethod(access, name, desc, exceptions, attrs);
    }

    public void setTarget(ClassVisitor target) {
        pass.setTarget(target);
        direct = target;
    }
}
