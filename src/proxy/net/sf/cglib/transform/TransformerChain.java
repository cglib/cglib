package net.sf.cglib.transform;

import org.objectweb.asm.*;

public class TransformerChain extends AbstractTransformer {
    private ClassTransformer[] chain;
    
    public TransformerChain(ClassTransformer[] chain) {
        this.chain = (ClassTransformer[])chain.clone();
    }

    public void setTarget(ClassVisitor v, ClassVisitor outer) {
        super.setTarget(chain[0], outer);
        ClassVisitor next = v;
        for (int i = chain.length - 1; i >= 0; i--) {
            chain[i].setTarget(next, outer);
            next = chain[i];
        }
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
