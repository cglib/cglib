package net.sf.cglib.transform;

import org.objectweb.asm.*;

public class MethodFilterTransformer extends AbstractFilterTransformer {
    private MethodFilter filter;
    private boolean accepted;
    
    public MethodFilterTransformer(MethodFilter filter, ClassTransformer pass) {
        super(pass);
        this.filter = filter;
    }

    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
        return (filter.accept(access, name, desc, exceptions) ? pass : cv).visitMethod(access, name, desc, exceptions);
    }
}
