package net.sf.cglib.transform;

import net.sf.cglib.core.ClassGenerator;
import org.objectweb.asm.ClassVisitor;

public class TransformingGenerator implements ClassGenerator {
    private ClassGenerator gen;
    private ClassTransformer t;
    
    public TransformingGenerator(ClassGenerator gen, ClassTransformer t) {
        this.gen = gen;
        this.t = t;
    }
    
    public void generateClass(ClassVisitor v) throws Exception {
        t.setTarget(v);
        gen.generateClass(t);
    }
}
