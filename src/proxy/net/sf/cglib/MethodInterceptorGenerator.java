package net.sf.cglib;

import java.lang.reflect.*;
import java.util.*;
import net.sf.cglib.util.*;

class MethodInterceptorGenerator
implements CallbackGenerator
{
    public static final MethodInterceptorGenerator INSTANCE = new MethodInterceptorGenerator();

    private static final int PRIVATE_FINAL_STATIC = Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC;
    private static final Method MAKE_PROXY =
      ReflectUtils.findMethod("MethodProxy.create(Method,Method)");
    private static final Method AROUND_ADVICE =
      ReflectUtils.findMethod("MethodInterceptor.intercept(Object, Method, Object[], MethodProxy)");

    public void generate(CodeGenerator cg, List methods, Context context) {
        for (int i = 0, size = methods.size(); i < size; i++) {
            Method method = (Method)methods.get(i);
            String accessName = getAccessName(method, i);
            String fieldName = getFieldName(i);

            cg.declare_field(PRIVATE_FINAL_STATIC, Method.class, fieldName);
            cg.declare_field(PRIVATE_FINAL_STATIC, MethodProxy.class, accessName);
            generateAccessMethod(cg, method, accessName);
            generateAroundMethod(cg, method, accessName, fieldName, context);
        }
    }

    protected void unbox(CodeGenerator cg, Class type) {
        cg.unbox_or_zero(type);
    }
    
    private static String getFieldName(int index) {
        return "CGLIB$MI$METHOD_" + index;
    }
    
    private static String getAccessName(Method method, int index) {
        return "CGLIB$MI$ACCESS_" + index + "_" + method.getName();
    }

    private void generateAccessMethod(CodeGenerator cg, Method method, String accessName) {
        cg.begin_method(Modifier.FINAL,
                        method.getReturnType(),
                        accessName,
                        method.getParameterTypes(),
                        method.getExceptionTypes());
        if (Modifier.isAbstract(method.getModifiers())) {
            cg.throw_exception(AbstractMethodError.class, method.toString() + " is abstract" );
        } else {
            cg.load_this();
            cg.load_args();
            cg.super_invoke(method);
        }
        cg.return_value();
        cg.end_method();
    }

    private void generateAroundMethod(CodeGenerator cg,
                                      Method method,
                                      String accessName,
                                      String fieldName,
                                      Context context) {
        cg.begin_method(method, context.getModifiers(method));
        
        Block handler = cg.begin_block();
        Label nullInterceptor = cg.make_label();
        context.emitCallback();
        cg.dup();
        cg.ifnull(nullInterceptor);

        cg.load_this();
        cg.getfield(fieldName);
        cg.create_arg_array();
        cg.getfield(accessName);
        cg.invoke(AROUND_ADVICE);
        unbox(cg, method.getReturnType());
        cg.return_value();

        cg.mark(nullInterceptor);
        cg.load_this();
        cg.load_args();
        cg.super_invoke(method);
        cg.return_value();

        cg.end_block();
        generateHandleUndeclared(cg, method, handler);
        cg.end_method();
    }

    private void generateHandleUndeclared(CodeGenerator cg, Method method, Block handler) {
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
            cg.invoke_constructor(UndeclaredThrowableException.class, Constants.TYPES_THROWABLE);
            cg.athrow();
        }
    }

    public void generateStatic(CodeGenerator cg, List methods) {
        /* generates:
           static {
             Class [] args;
             Class cls = findClass("java.lang.Object");
             args = new Class[0];
             METHOD_1 = cls.getDeclaredMethod("toString", args);

             Class thisClass = findClass("NameOfThisClass");
             Method proxied = thisClass.getDeclaredMethod("CGLIB$ACCESS_O", args);
             CGLIB$ACCESS_0 = MethodProxy.create(proxied);
           }
        */

        Local args = cg.make_local();
        for (int i = 0, size = methods.size(); i < size; i++) {
            Method method = (Method)methods.get(i);
            String fieldName = getFieldName(i);

            cg.load_class(method.getDeclaringClass());
            cg.push(method.getName());
            cg.push_object(method.getParameterTypes());
            cg.dup();
            cg.store_local(args);
            cg.invoke(MethodConstants.GET_DECLARED_METHOD);
            cg.dup();
            cg.putfield(fieldName);

            String accessName = getAccessName(method, i);
            cg.load_class_this();
            cg.push(accessName);
            cg.load_local(args);
            cg.invoke(MethodConstants.GET_DECLARED_METHOD);
            
            cg.invoke(MAKE_PROXY);
            cg.putfield(accessName);
        }
    }
}
