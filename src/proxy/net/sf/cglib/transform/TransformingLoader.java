package net.sf.cglib.transform;

import java.util.*;
import net.sf.cglib.core.ClassGenerator;
import org.objectweb.asm.*;

public class TransformingLoader extends AbstractLoader {
    
    private ClassTransformerFactory t;
    
    public interface ClassTransformerFactory{
    
        ClassTransformer newInstance();
    
    }
    
    public TransformingLoader(ClassLoader parent, ClassFilter filter, ClassTransformerFactory t) {
        super(parent, parent, filter);
        this.t = t;
    }

    protected ClassGenerator getGenerator(ClassReader r) {
        ClassTransformer t2 = (ClassTransformer)t.newInstance();
        return new TransformingGenerator(super.getGenerator(r), t2);
    }
}
