package net.sf.cglib.transform;

import net.sf.cglib.core.CodeGenerationException;
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Label;

abstract public class CodeTransformer extends CodeAdapter implements Cloneable {   
    protected CodeTransformer() {
        super(null);
    }
    
    public void setTarget(CodeVisitor target) {
        cv = target;
    }

    public CodeVisitor getTarget() {
        return cv;
    }

    public ClassTransformer asClassTransformer() {
        return new ClassTransformer() {
            public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
                CodeVisitor inner = super.visitMethod(access, name, desc, exceptions);
                CodeTransformer ct = (CodeTransformer)CodeTransformer.this.clone();
                ct.setTarget(inner);
                return ct;
            }
        };
    }

    public Object clone() {
        try {
            CodeTransformer t = (CodeTransformer)super.clone();
            if (cv instanceof CodeTransformer) {
                t.cv = (CodeTransformer)((CodeTransformer)cv).clone();
            }
            return t;
        } catch (CloneNotSupportedException e) {
            throw new CodeGenerationException(e); // should be impossible
        }
    }
}
