/*
 * Copyright 2003,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.proxy;

import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

class MethodInterceptorGenerator
implements CallbackGenerator
{
    public static final MethodInterceptorGenerator INSTANCE = new MethodInterceptorGenerator();

    static final String EMPTY_ARGS_NAME = "CGLIB$emptyArgs";
    static final String FIND_PROXY_NAME = "CGLIB$findMethodProxy";
    static final Class[] FIND_PROXY_TYPES = { Signature.class };

    private static final Type ABSTRACT_METHOD_ERROR =
      TypeUtils.parseType("AbstractMethodError");
    private static final Type METHOD =
      TypeUtils.parseType("java.lang.reflect.Method");
    private static final Type METHOD_PROXY =
      TypeUtils.parseType("net.sf.cglib.proxy.MethodProxy");
    private static final Type METHOD_INTERCEPTOR =
      TypeUtils.parseType("net.sf.cglib.proxy.MethodInterceptor");
    private static final Signature GET_DECLARING_CLASS =
      TypeUtils.parseSignature("Class getDeclaringClass()");
    private static final Signature GET_CLASS_LOADER =
      TypeUtils.parseSignature("ClassLoader getClassLoader()");
    private static final Signature MAKE_PROXY =
      new Signature("create", METHOD_PROXY, new Type[]{
          Constants.TYPE_CLASS_LOADER,
          Constants.TYPE_CLASS,
          Constants.TYPE_CLASS,
          Constants.TYPE_STRING,
          Constants.TYPE_STRING,
          Constants.TYPE_STRING
      });
    private static final Signature INTERCEPT =
      new Signature("intercept", Constants.TYPE_OBJECT, new Type[]{
          Constants.TYPE_OBJECT,
          METHOD,
          Constants.TYPE_OBJECT_ARRAY,
          METHOD_PROXY
      });
    private static final Signature FIND_PROXY =
      new Signature(FIND_PROXY_NAME, METHOD_PROXY, new Type[]{ Constants.TYPE_SIGNATURE });
    private static final Signature TO_STRING =
      TypeUtils.parseSignature("String toString()");

    private String getMethodField(Signature impl) {
        return impl.getName() + "$Method";
    }
    private String getMethodProxyField(Signature impl) {
        return impl.getName() + "$Proxy";
    }

    public void generate(ClassEmitter ce, Context context, List methods) {
        Map sigMap = new HashMap();
        for (Iterator it = methods.iterator(); it.hasNext();) {
            MethodInfo method = (MethodInfo)it.next();
            Signature sig = method.getSignature();
            Signature impl = context.getImplSignature(method);

            String methodField = getMethodField(impl);
            String methodProxyField = getMethodProxyField(impl);

            sigMap.put(sig.toString(), methodProxyField);
            ce.declare_field(Constants.PRIVATE_FINAL_STATIC, methodField, METHOD, null, null);
            ce.declare_field(Constants.PRIVATE_FINAL_STATIC, methodProxyField, METHOD_PROXY, null, null);
            ce.declare_field(Constants.PRIVATE_FINAL_STATIC, EMPTY_ARGS_NAME, Constants.TYPE_OBJECT_ARRAY, null, null);
            CodeEmitter e;

            // access method
            e = ce.begin_method(Constants.ACC_FINAL,
                                impl,
                                method.getExceptionTypes(),
                                null);
            if (TypeUtils.isAbstract(method.getModifiers())) {
                e.throw_exception(ABSTRACT_METHOD_ERROR, method.toString() + " is abstract" );
            } else {
                e.load_this();
                e.load_args();
                e.super_invoke(sig);
            }
            e.return_value();
            e.end_method();

            // around method
            e = context.beginMethod(ce, method);
            Label nullInterceptor = e.make_label();
            Label haveMethod = e.make_label();
            context.emitCallback(e, context.getIndex(method));
            e.dup();
            e.ifnull(nullInterceptor);

            e.load_this();
            e.getfield(methodField);
            e.dup();
            e.ifnonnull(haveMethod);
            
            e.pop();
            EmitUtils.load_class_this(e);
            e.dup();
            e.invoke_virtual(Constants.TYPE_CLASS, GET_CLASS_LOADER);
            e.swap();
            EmitUtils.load_method(e, method);
            e.dup();
            e.putfield(getMethodField(impl));
            e.invoke_virtual(METHOD, GET_DECLARING_CLASS);
            e.swap();
            e.push(sig.getDescriptor());
            e.push(sig.getName());
            e.push(impl.getName());
            e.invoke_static(METHOD_PROXY, MAKE_PROXY);
            e.putfield(getMethodProxyField(impl));
            e.getfield(methodField);
            e.mark(haveMethod);
            
            if (sig.getArgumentTypes().length == 0) {
                e.getfield(EMPTY_ARGS_NAME);
            } else {
                e.create_arg_array();
            }
            
            e.getfield(methodProxyField);
            e.invoke_interface(METHOD_INTERCEPTOR, INTERCEPT);
            e.unbox_or_zero(sig.getReturnType());
            e.return_value();

            e.mark(nullInterceptor);
            e.load_this();
            e.load_args();
            e.super_invoke(sig);
            e.return_value();
            e.end_method();
        }
        generateFindProxy(ce, sigMap);
    }

    public void generateStatic(CodeEmitter e, Context context, List methods) {
        e.push(0);
        e.newarray();
        e.putfield(EMPTY_ARGS_NAME);        
    }

    public void generateFindProxy(ClassEmitter ce, final Map sigMap) {
        final CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
                                              FIND_PROXY,
                                              null,
                                              null);
        e.load_arg(0);
        e.invoke_virtual(Constants.TYPE_OBJECT, TO_STRING);
        ObjectSwitchCallback callback = new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
                e.getfield((String)sigMap.get(key));
                e.return_value();
            }
            public void processDefault() {
                e.aconst_null();
                e.return_value();
            }
        };
        EmitUtils.string_switch(e,
                                (String[])sigMap.keySet().toArray(new String[0]),
                                Constants.SWITCH_STYLE_HASH,
                                callback);
        e.end_method();
    }
}
