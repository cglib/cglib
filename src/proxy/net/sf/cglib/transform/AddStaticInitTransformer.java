package net.sf.cglib.transform;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.sf.cglib.core.*;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.Type;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class AddStaticInitTransformer extends EmittingTransformer {
    private static final Signature FOR_NAME =
      TypeUtils.parseSignature("Class forName(String)");

    private Method classInit;
    private boolean generated;

    public AddStaticInitTransformer(Method classInit) {
        if (!Modifier.isStatic(classInit.getModifiers())) {
            throw new IllegalArgumentException(classInit + " is not static");
        }
        Class[] types = classInit.getParameterTypes();
        if (types.length != 1 ||
            !types[0].equals(Class.class) ||
            !classInit.getReturnType().equals(Void.TYPE)) {
            throw new IllegalArgumentException(classInit + " illegal signature");
        }
        this.classInit = classInit;
    }

    protected void init() {
        generated = false; // transformers can be cloned, need to reset
    }
                
    public CodeEmitter begin_method(int access, Signature sig, Type[] exceptions) {
        CodeEmitter e = super.begin_method(access, sig, exceptions);
        if (sig.getName().equals(Constants.STATIC_NAME)) {
            generated = true;
            ComplexOps.load_class_this(e);
            e.invoke(classInit);
        }
        return e;
    }

    public void end_class() {
        if (!generated) {
            CodeEmitter e = begin_static(); // calls begin_method
            e.return_value();
            e.end_method();
        }
        super.end_class();
    }
}
