package net.sf.cglib;

import net.sf.cglib.core.*;
import java.lang.reflect.Method;
import java.util.*;

class InvocationHandlerGenerator
implements CallbackGenerator
{
    public static final InvocationHandlerGenerator INSTANCE = new InvocationHandlerGenerator();

    private static final Method INVOKE =
      ReflectUtils.findMethod("net.sf.cglib.InvocationHandler.invoke(Object, Method, Object[])");

    private String getFieldName(Context context, Method method) {
        return "CGLIB$$METHOD_" + context.getUniqueName(method);
    }

    public void generate(Emitter cg, Context context) {
        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();

            String fieldName = getFieldName(context, method);
            cg.declare_field(Constants.PRIVATE_FINAL_STATIC, Method.class, fieldName);

            cg.begin_method(method, context.getModifiers(method));
            Block handler = cg.begin_block();
            context.emitCallback();
            cg.load_this();
            cg.getfield(fieldName);
            Virt.create_arg_array(cg);
            cg.invoke(INVOKE);
            Virt.unbox(cg, method.getReturnType());
            cg.return_value();
            cg.end_block();
            Virt.handle_undeclared(cg, method.getExceptionTypes(), handler);
            cg.end_method();
        }
    }

    public void generateStatic(Emitter cg, Context context) {
        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();
            Virt.load_method(cg, method);
            cg.putfield(getFieldName(context, method));
        }
    }
}
