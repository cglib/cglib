package net.sf.cglib.transform;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.sf.cglib.core.*;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class AddStaticInitTransformer extends EmittingTransformer {
    private Method classInit;

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

    public void end_class() {
        if (!TypeUtils.isInterface(getAccess())) {
            CodeEmitter e = getStaticHook();
            ComplexOps.load_class_this(e);
            e.invoke(classInit);
        }
        super.end_class();
    }
}
