package net.sf.cglib;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import net.sf.cglib.util.*;

class LazyLoaderGenerator
implements CallbackGenerator
{
    public static final LazyLoaderGenerator INSTANCE = new LazyLoaderGenerator();

    private static final String DELEGATE = "CGLIB$LAZY_LOADER";
    private static final String LOAD_PRIVATE = "CGLIB$LOAD_PRIVATE";
    private static final Method LOAD_OBJECT =
      ReflectUtils.findMethod("LazyLoader.loadObject()");

    public void generate(CodeGenerator cg, List methods, Context context) {
        cg.declare_field(Modifier.PRIVATE, Object.class, DELEGATE);

        cg.begin_method(Modifier.PRIVATE | Modifier.SYNCHRONIZED | Modifier.FINAL,
                        Object.class,
                        LOAD_PRIVATE,
                        null,
                        null);
        cg.load_this();
        cg.getfield(DELEGATE);
        cg.dup();
        Label end = cg.make_label();
        cg.ifnonnull(end);
        cg.pop();
        cg.load_this();
        context.emitCallback();
        cg.checkcast(LazyLoader.class);
        cg.invoke(LOAD_OBJECT);
        cg.dup_x1();
        cg.putfield(DELEGATE);
        cg.mark(end);
        cg.return_value();
        cg.end_method();

        for (Iterator it = methods.iterator(); it.hasNext();) {
            Method method = (Method)it.next();
            if (Modifier.isProtected(method.getModifiers())) {
                // ignore protected methods
            } else {
                cg.begin_method(method, context.getModifiers(method));
                cg.load_this();
                cg.dup();
                cg.invoke_virtual_this(LOAD_PRIVATE, Object.class, null);
                cg.checkcast(method.getDeclaringClass());
                cg.load_args();
                cg.invoke(method);
                cg.return_value();
                cg.end_method();
            }
        }
    }

    public void generateStatic(CodeGenerator cg, List methods) { }
}
