package net.sf.cglib;

import net.sf.cglib.core.*;
import java.lang.reflect.Method;
import java.util.*;
import org.objectweb.asm.Type;

class InvocationHandlerGenerator
implements CallbackGenerator
{
    public static final InvocationHandlerGenerator INSTANCE = new InvocationHandlerGenerator();

    private static final Type INVOCATION_HANDLER =
      Signature.parseType("net.sf.cglib.InvocationHandler");
    public static final Type UNDECLARED_THROWABLE_EXCEPTION =
      Signature.parseType("net.sf.cglib.UndeclaredThrowableException");
    private static final Signature CSTRUCT_THROWABLE =
      Signature.parse("void <init>(Throwable)");
    private static final Signature INVOKE =
      Signature.parse("Object invoke(Object, java.lang.reflect.Method, Object[])");

    private String getFieldName(Context context, Method method) {
        return "CGLIB$$METHOD_" + context.getUniqueName(method);
    }

    public void generate(Emitter e, Context context) {
        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();

            String fieldName = getFieldName(context, method);
            e.declare_field(Constants.PRIVATE_FINAL_STATIC, fieldName, Constants.TYPE_METHOD, null);

            ReflectOps.begin_method(e, method, context.getModifiers(method));
            Block handler = e.begin_block();
            context.emitCallback();
            e.load_this();
            e.getfield(fieldName);
            Ops.create_arg_array(e);
            e.invoke_interface(INVOCATION_HANDLER, INVOKE);
            Ops.unbox(e, Type.getType(method.getReturnType()));
            e.return_value();
            e.end_block();
            handle_undeclared(e, method.getExceptionTypes(), handler);
        }
    }

    public void generateStatic(Emitter e, Context context) {
        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();
            ReflectOps.load_method(e, method);
            e.putfield(getFieldName(context, method));
        }
    }

    private static void handle_undeclared(Emitter e, Class[] exceptionTypes, Block handler) {
        /* generates:
           } catch (RuntimeException e) {
               throw e;
           } catch (Error e) {
               throw e;
           } catch (<DeclaredException> e) {
               throw e;
           } catch (Throwable e) {
               throw new UndeclaredThrowableException(e);
           }
        */
        Set exceptionSet = new HashSet(Arrays.asList(exceptionTypes));
        if (!(exceptionSet.contains(Exception.class) ||
              exceptionSet.contains(Throwable.class))) {
            if (!exceptionSet.contains(RuntimeException.class)) {
                e.catch_exception(handler, Constants.TYPE_RUNTIME_EXCEPTION);
                e.athrow();
            }
            if (!exceptionSet.contains(Error.class)) {
                e.catch_exception(handler, Constants.TYPE_ERROR);
                e.athrow();
            }
            for (int i = 0; i < exceptionTypes.length; i++) {
                e.catch_exception(handler, Type.getType(exceptionTypes[i]));
                e.athrow();
            }
            // e -> eo -> oeo -> ooe -> o
            e.catch_exception(handler, Constants.TYPE_THROWABLE);
            e.new_instance(UNDECLARED_THROWABLE_EXCEPTION);
            e.dup_x1();
            e.swap();
            e.invoke_constructor(UNDECLARED_THROWABLE_EXCEPTION, CSTRUCT_THROWABLE);
            e.athrow();
        }
    }
}
