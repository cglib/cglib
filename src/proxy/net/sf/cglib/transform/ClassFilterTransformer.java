package net.sf.cglib.transform;

import org.objectweb.asm.*;

public class ClassFilterTransformer extends AbstractClassFilterTransformer {
    private ClassFilter filter;

    public ClassFilterTransformer(ClassFilter filter, ClassTransformer pass) {
        super(pass);
        this.filter = filter;
    }

    protected boolean accept(int access, String name, String superName, String[] interfaces, String sourceFile) {
        return filter.accept(name.replace('/', '.'));
    }
}
