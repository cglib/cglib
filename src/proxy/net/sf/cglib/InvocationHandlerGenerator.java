package net.sf.cglib;

import net.sf.cglib.core.*;
import java.lang.reflect.Method;
import java.util.*;

class InvocationHandlerGenerator
implements CallbackGenerator
{
    public static final InvocationHandlerGenerator INSTANCE = new InvocationHandlerGenerator();

    private static final Class[] TYPES_THROWABLE = { Throwable.class };
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
            handle_undeclared(cg, method.getExceptionTypes(), handler);
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

    private static void handle_undeclared(Emitter cg, Class[] exceptionTypes, Block handler) {
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
                cg.catch_exception(handler, RuntimeException.class);
                cg.athrow();
            }
            if (!exceptionSet.contains(Error.class)) {
                cg.catch_exception(handler, Error.class);
                cg.athrow();
            }
            for (int i = 0; i < exceptionTypes.length; i++) {
                cg.catch_exception(handler, exceptionTypes[i]);
                cg.athrow();
            }
            // e -> eo -> oeo -> ooe -> o
            cg.catch_exception(handler, Throwable.class);
            cg.new_instance(UndeclaredThrowableException.class);
            cg.dup_x1();
            cg.swap();
            cg.invoke_constructor(UndeclaredThrowableException.class, TYPES_THROWABLE);
            cg.athrow();
        }
    }
}
