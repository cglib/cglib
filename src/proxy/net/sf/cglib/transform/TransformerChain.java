package net.sf.cglib.transform;

import org.objectweb.asm.*;

public class TransformerChain extends AbstractTransformer {
    private ClassTransformer[] chain;
    
    public TransformerChain(ClassTransformer[] chain) {
        this.chain = cloneAndLink(chain);
    }

    private static ClassTransformer[] cloneAndLink(ClassTransformer[] chain) {
        ClassTransformer[] copy = new ClassTransformer[chain.length];
        ClassVisitor cv = null;
        for (int i = chain.length - 1; i >= 0; i--) {
            copy[i] = (ClassTransformer)chain[i].clone();
            copy[i].setTarget(cv);
            cv = copy[i];
        }
        return copy;
    }

    public void setTarget(ClassVisitor v) {
        super.setTarget(chain[0]);
        chain[chain.length - 1].setTarget(v);
    }

    public Object clone() {
        TransformerChain t = (TransformerChain)super.clone();
        t.chain = cloneAndLink(chain);
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
