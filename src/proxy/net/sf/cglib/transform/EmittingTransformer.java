package net.sf.cglib.transform;

import net.sf.cglib.core.*;

abstract public class EmittingTransformer extends ClassEmitter implements ClassTransformer {
    public Object clone() {
        try {
            EmittingTransformer t = (EmittingTransformer)super.clone();
            if (cv instanceof ClassTransformer) {
                t.cv = (ClassTransformer)((ClassTransformer)cv).clone();
            }
            return t;
        } catch (CloneNotSupportedException e) {
            throw new CodeGenerationException(e); // should be impossible
        }
    }
}
