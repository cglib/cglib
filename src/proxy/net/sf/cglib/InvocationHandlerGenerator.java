package net.sf.cglib;

import net.sf.cglib.util.*;
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

    public void generate(CodeGenerator cg, Context context) {
        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();

            String fieldName = getFieldName(context, method);
            cg.declare_field(Constants.PRIVATE_FINAL_STATIC, Method.class, fieldName);

            cg.begin_method(method, context.getModifiers(method));
            Block handler = cg.begin_block();
            context.emitCallback();
            cg.load_this();
            cg.getfield(fieldName);
            cg.create_arg_array();
            cg.invoke(INVOKE);
            cg.unbox(method.getReturnType());
            cg.return_value();
            cg.end_block();
            cg.handle_undeclared(method.getExceptionTypes(), handler);
            cg.end_method();
        }
    }

    public void generateStatic(CodeGenerator cg, Context context) {
        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();
            cg.load_method(method);
            cg.putfield(getFieldName(context, method));
        }
    }
}
