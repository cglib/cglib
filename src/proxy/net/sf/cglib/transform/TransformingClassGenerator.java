package net.sf.cglib.transform;

import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.Transformer;
import org.objectweb.asm.ClassVisitor;

public class TransformingClassGenerator implements ClassGenerator {
    private ClassGenerator gen;
    private ClassTransformer t;
    
    public TransformingClassGenerator(ClassGenerator gen, ClassTransformer t) {
        this.gen = gen;
        this.t = t;
    }
    
    public void generateClass(ClassVisitor v) throws Exception {
        t.setTarget(v);
        gen.generateClass(t);
    }
}
