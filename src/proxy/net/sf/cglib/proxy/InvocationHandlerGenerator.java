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
    private static final Signature CSTRUCT_THROWABLE =
      TypeUtils.parseConstructor("Throwable");

    private String getFieldName(Context context, Method method) {
        return "CGLIB$$METHOD_" + context.getUniqueName(method);
    }

    public void generate(ClassEmitter ce, final Context context) {
        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();

            String fieldName = getFieldName(context, method);
            ce.declare_field(Constants.PRIVATE_FINAL_STATIC, fieldName, METHOD, null, null);

            CodeEmitter e = ce.begin_method(context.getModifiers(method),
                                            ReflectUtils.getSignature(method),
                                            ReflectUtils.getExceptionTypes(method),
                                            null);
            Block handler = e.begin_block();
            context.emitCallback(e);
            e.load_this();
            e.getfield(fieldName);
            e.create_arg_array();
            e.invoke_interface(INVOCATION_HANDLER, INVOKE);
            e.unbox(Type.getType(method.getReturnType()));
            e.return_value();
            handler.end();

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
            Class[] exceptionTypes = method.getExceptionTypes();
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
