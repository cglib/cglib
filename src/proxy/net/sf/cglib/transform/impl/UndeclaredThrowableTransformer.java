package net.sf.cglib.transform.impl;

import java.lang.reflect.Constructor;
import net.sf.cglib.core.*;
import net.sf.cglib.transform.*;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Type;
import org.objectweb.asm.ClassVisitor;

public class UndeclaredThrowableTransformer extends ClassEmitterTransformer {
    private Type wrapper;

    public UndeclaredThrowableTransformer(Class wrapper) {
        this.wrapper = Type.getType(wrapper);
        boolean found = false;
        Constructor[] cstructs = wrapper.getConstructors();
        for (int i = 0; i < cstructs.length; i++) {
            Class[] types = cstructs[i].getParameterTypes();
            if (types.length == 1 && types[0].equals(Throwable.class)) {
                found = true;
                break;
            }
        }
        if (!found)
            throw new IllegalArgumentException(wrapper + " does not have a single-arg constructor that takes a Throwable");
    }

    public CodeEmitter begin_method(int access, final Signature sig, final Type[] exceptions, Attribute attrs) {
        CodeEmitter e = super.begin_method(access, sig, exceptions, attrs);
        if (TypeUtils.isAbstract(access) || sig.equals(Constants.SIG_STATIC)) {
            return e;
        }
        return new CodeEmitter(e) {
            private Block handler;
            /* init */ {
                handler = begin_block();
            }
            public void visitMaxs(int maxStack, int maxLocals) {
                handler.end();
                EmitUtils.wrap_undeclared_throwable(this, handler, exceptions, wrapper);
                super.visitMaxs(maxStack, maxLocals);
            }
        };
    }
}
