package net.sf.cglib.transform;

import org.objectweb.asm.*;

public class ClassTransformerChain extends AbstractClassTransformer {
    private ClassTransformer[] chain;
    
    public ClassTransformerChain(ClassTransformer[] chain) {
        this.chain = (ClassTransformer[])chain.clone();
    }

    public void setTarget(ClassVisitor v) {
        super.setTarget(chain[0]);
        ClassVisitor next = v;
        for (int i = chain.length - 1; i >= 0; i--) {
            chain[i].setTarget(next);
            next = chain[i];
        }
    }

    public CodeVisitor visitMethod(int access,
                                   String name,
                                   String desc,
                                   String[] exceptions,
                                   Attribute attrs) {
        return cv.visitMethod(access, name, desc, exceptions, attrs);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ClassTransformerChain{");
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
