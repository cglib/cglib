package net.sf.cglib.transform;

import org.objectweb.asm.*;

public class TransformerChain extends ClassTransformer {
    private ClassTransformer last;
    private int size;
    
    public TransformerChain(ClassTransformer[] transformers) {
        size = transformers.length;
        last = transformers[size - 1];
        for (int i = size - 1; i >= 0; i--) {
            transformers[i].wire(cv);
            cv = transformers[i];
        }
    }

    public void wire(ClassVisitor v) {
        last.wire(v);
    }

    public Object clone() {
        TransformerChain t = (TransformerChain)super.clone();
        t.last = t;
        for (int i = 0; i < size; i++) {
            t.last = (ClassTransformer)t.last.cv;
        }
        return t;
    }
}
