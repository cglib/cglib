/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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

    private static final Signature FIND_PROXY =
      new Signature(FIND_PROXY_NAME,
                    TypeUtils.parseType("net.sf.cglib.proxy.MethodProxy"),
                    TypeUtils.getTypes(FIND_PROXY_TYPES));

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
      TypeUtils.parseSignature("net.sf.cglib.proxy.MethodProxy create(ClassLoader, Class, Class, String, String, String)");
    private static final Signature INTERCEPT =
      TypeUtils.parseSignature("Object intercept(Object, java.lang.reflect.Method, Object[], net.sf.cglib.proxy.MethodProxy)");
    private static final Signature TO_STRING =
      TypeUtils.parseSignature("String toString()");

    private String getMethodField(Signature impl) {
        return impl.getName() + "$Method";
    }
    private String getMethodProxyField(Signature impl) {
        return impl.getName() + "$Proxy";
    }

    public void generate(ClassEmitter ce, Context context) {
        Map sigMap = new HashMap();
        for (Iterator it = context.getMethods(); it.hasNext();) {
            MethodInfo method = (MethodInfo)it.next();
            Signature impl = context.getImplSignature(method);

            String methodField = getMethodField(impl);
            String methodProxyField = getMethodProxyField(impl);

            sigMap.put(method.getSignature().toString(), methodProxyField);
            ce.declare_field(Constants.PRIVATE_FINAL_STATIC, methodField, METHOD, null, null);
            ce.declare_field(Constants.PRIVATE_FINAL_STATIC, methodProxyField, METHOD_PROXY, null, null);
            ce.declare_field(Constants.PRIVATE_FINAL_STATIC, EMPTY_ARGS_NAME, Constants.TYPE_OBJECT_ARRAY, null, null);
            CodeEmitter e;

            // access method
            if (!context.isTransforming()) {
                e = ce.begin_method(Constants.ACC_FINAL,
                                    impl,
                                    method.getExceptionTypes(),
                                    null);
                if (TypeUtils.isAbstract(method.getModifiers())) {
                    e.throw_exception(ABSTRACT_METHOD_ERROR, method.toString() + " is abstract" );
                } else {
                    e.load_this();
                    e.load_args();
                    e.super_invoke(method.getSignature());
                }
                e.return_value();
                e.end_method();
            }

            // around method
            e = EmitUtils.begin_method(ce, method);
            Label nullInterceptor = e.make_label();
            context.emitCallback(e, context.getIndex(method));
            e.dup();
            e.ifnull(nullInterceptor);

            e.load_this();
            e.getfield(methodField);

            if (method.getSignature().getArgumentTypes().length == 0) {
                e.getfield(EMPTY_ARGS_NAME);
            } else {
                e.create_arg_array();
            }
            
            e.getfield(methodProxyField);
            e.invoke_interface(METHOD_INTERCEPTOR, INTERCEPT);
            e.unbox_or_zero(method.getSignature().getReturnType());
            e.return_value();

            e.mark(nullInterceptor);
            e.load_this();
            e.load_args();
            if (context.isTransforming()) {
                e.invoke_virtual_this(impl);
            } else {
                e.super_invoke(method.getSignature());
            }
            e.return_value();
            e.end_method();
        }
        generateFindProxy(ce, sigMap);
    }

    public void generateStatic(CodeEmitter e, final Context context) {
        /* generates:
           static {
             Class cls = findClass("java.lang.Object");
             METHOD_1 = cls.getDeclaredMethod("toString", new Class[0]);

             Class thisClass = findClass("NameOfThisClass");
             CGLIB$ACCESS_0 = MethodProxy.create(thisClass.getClassLoader(), cls, thisClass, "()Ljava/lang/String;", "toString", "CGLIB$ACCESS_0");
           }
        */

        Local thisclass = e.make_local();
        EmitUtils.load_class_this(e);
        e.dup();
        e.store_local(thisclass);
        e.invoke_virtual(Constants.TYPE_CLASS, GET_CLASS_LOADER);

        e.push(0);
        e.newarray();
        e.putfield(EMPTY_ARGS_NAME);
        
        for (Iterator it = context.getMethods(); it.hasNext();) {
            e.dup();
            MethodInfo method = (MethodInfo)it.next();
            Signature impl = context.getImplSignature(method);
            EmitUtils.load_method(e, method);
            e.dup();
            e.putfield(getMethodField(impl));

            Signature sig = method.getSignature();
            e.invoke_virtual(METHOD, GET_DECLARING_CLASS);
            e.load_local(thisclass);
            e.push(sig.getDescriptor());
            e.push(sig.getName());
            e.push(impl.getName());
            e.invoke_static(METHOD_PROXY, MAKE_PROXY);
            e.putfield(getMethodProxyField(impl));
        }
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
