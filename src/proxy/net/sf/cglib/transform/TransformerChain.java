package net.sf.cglib.transform;

import org.objectweb.asm.*;

public class TransformerChain extends AbstractTransformer {
    private ClassTransformer[] chain;
    
    public TransformerChain(ClassTransformer[] chain) {
        this.chain = clone(chain);
    }

    private static ClassTransformer[] clone(ClassTransformer[] chain) {
        ClassTransformer[] copy = new ClassTransformer[chain.length];
        for (int i = chain.length - 1; i >= 0; i--) {
            copy[i] = (ClassTransformer)chain[i].clone();
        }
        return copy;
    }

    public void setTarget(ClassVisitor v, ClassVisitor outer) {
        super.setTarget(chain[0], outer);
        ClassVisitor next = v;
        for (int i = chain.length - 1; i >= 0; i--) {
            chain[i].setTarget(next, outer);
            next = chain[i];
        }
    }

    public Object clone() {
        TransformerChain t = (TransformerChain)super.clone();
        t.chain = clone(chain);
        return t;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("TransformerChain{");
        for (int i = 0; i < chain.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(chain[i].toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
