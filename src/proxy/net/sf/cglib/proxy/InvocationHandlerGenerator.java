package net.sf.cglib.proxy;

import net.sf.cglib.core.*;
import java.lang.reflect.Method;
import java.util.*;
import org.objectweb.asm.Type;

class InvocationHandlerGenerator
implements CallbackGenerator
{
    public static final InvocationHandlerGenerator INSTANCE = new InvocationHandlerGenerator();

    private static final Type INVOCATION_HANDLER =
      TypeUtils.parseType("net.sf.cglib.proxy.InvocationHandler");
    private static final Type UNDECLARED_THROWABLE_EXCEPTION =
      TypeUtils.parseType("net.sf.cglib.proxy.UndeclaredThrowableException");
    private static final Type METHOD =
      TypeUtils.parseType("java.lang.reflect.Method");
    private static final Signature INVOKE =
      TypeUtils.parseSignature("Object invoke(Object, java.lang.reflect.Method, Object[])");

    private String getFieldName(Context context, Method method) {
        return "CGLIB$$METHOD_" + context.getUniqueName(method);
    }

    public void generate(ClassEmitter ce, final Context context) {
        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();

            String fieldName = getFieldName(context, method);
            ce.declare_field(Constants.PRIVATE_FINAL_STATIC, fieldName, METHOD, null, null);

            Type[] exceptions = ReflectUtils.getExceptionTypes(method);
            CodeEmitter e = ce.begin_method(context.getModifiers(method),
                                            ReflectUtils.getSignature(method),
                                            exceptions,
                                            null);
            Block handler = e.begin_block();
            context.emitCallback(e, context.getIndex(method));
            e.load_this();
            e.getfield(fieldName);
            e.create_arg_array();
            e.invoke_interface(INVOCATION_HANDLER, INVOKE);
            e.unbox(Type.getType(method.getReturnType()));
            e.return_value();
            handler.end();
            EmitUtils.wrap_undeclared_throwable(e, handler, exceptions, UNDECLARED_THROWABLE_EXCEPTION);
            e.end_method();
        }
    }

    public void generateStatic(CodeEmitter e, final Context context) {
        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();
            EmitUtils.load_method(e, method);
            e.putfield(getFieldName(context, method));
        }
    }
}
