package net.sf.cglib.transform;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.sf.cglib.core.*;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.Type;
import org.objectweb.asm.ClassVisitor;

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

    protected Emitter getEmitter(ClassVisitor cv) {
        return new Emitter(cv) {
            public void begin_class(int access, String name, Type superType, Type[] interfaces, String sourceFile) {
                super.begin_class(access, name, superType, interfaces, sourceFile);
                generated = false; // transformers can be cloned, need to reset
            }
                
            public void begin_method(int access, Signature sig, Type[] exceptions) {
                super.begin_method(access, sig, exceptions);
                if (sig.getName().equals(Constants.STATIC_NAME)) {
                    generated = true;
                    ComplexOps.load_class_this(this);
                    ReflectOps.invoke(this, classInit);
                }
            }

            public void end_class() {
                if (!generated) {
                    begin_static(); // calls begin_method
                    return_value();
                }
                super.end_class();
            }
        };
    }
}
