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
package net.sf.cglib;

import java.lang.reflect.*;
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

class MethodInterceptorGenerator
implements CallbackGenerator
{
    public static final MethodInterceptorGenerator INSTANCE = new MethodInterceptorGenerator();

    private static final Type ABSTRACT_METHOD_ERROR =
      TypeUtils.parseType("AbstractMethodError");
    private static final Type METHOD =
      TypeUtils.parseType("java.lang.reflect.Method");
    private static final Type METHOD_PROXY =
      TypeUtils.parseType("net.sf.cglib.MethodProxy");
    private static final Type METHOD_INTERCEPTOR =
      TypeUtils.parseType("net.sf.cglib.MethodInterceptor");
    private static final Signature GET_DECLARING_CLASS =
      TypeUtils.parseSignature("Class getDeclaringClass()");
    private static final Signature GET_CLASS_LOADER =
      TypeUtils.parseSignature("ClassLoader getClassLoader()");
    private static final Signature MAKE_PROXY =
      TypeUtils.parseSignature("net.sf.cglib.MethodProxy create(ClassLoader ,Class, String, Class, String)");
    private static final Signature INTERCEPT =
      TypeUtils.parseSignature("Object intercept(Object, java.lang.reflect.Method, Object[], net.sf.cglib.MethodProxy)");

    public void generate(ClassEmitter ce, Context context) {
        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();
            String accessName = getAccessName(context, method);
            String fieldName = getFieldName(context, method);

            ce.declare_field(Constants.PRIVATE_FINAL_STATIC, fieldName, METHOD, null);
            ce.declare_field(Constants.PRIVATE_FINAL_STATIC, accessName, METHOD_PROXY, null);
            CodeEmitter e;

            // access method
            e = ce.begin_method(Constants.ACC_FINAL,
                                new Signature(getAccessName(context, method),
                                              ReflectUtils.getSignature(method).getDescriptor()),
                                ReflectUtils.getExceptionTypes(method));
            if (Modifier.isAbstract(method.getModifiers())) {
                e.throw_exception(ABSTRACT_METHOD_ERROR, method.toString() + " is abstract" );
            } else {
                e.load_this();
                e.load_args();
                e.super_invoke(ReflectUtils.getSignature(method));
            }
            e.return_value();
            e.end_method();

            // around method
            e = ce.begin_method(context.getModifiers(method),
                                ReflectUtils.getSignature(method),
                                ReflectUtils.getExceptionTypes(method));
            Label nullInterceptor = e.make_label();
            context.emitCallback(e);
            e.dup();
            e.ifnull(nullInterceptor);

            e.load_this();
            e.getfield(fieldName);
            e.create_arg_array();
            e.getfield(accessName);
            e.invoke_interface(METHOD_INTERCEPTOR, INTERCEPT);
            e.unbox_or_zero(Type.getType(method.getReturnType()));
            e.return_value();

            e.mark(nullInterceptor);
            e.load_this();
            e.load_args();
            e.super_invoke(ReflectUtils.getSignature(method));
            e.return_value();
            e.end_method();
        }
    }

    private String getFieldName(Context context, Method method) {
        return "CGLIB$$METHOD_" + context.getUniqueName(method);
    }
    
    private String getAccessName(Context context, Method method) {
        return "CGLIB$$ACCESS_" + context.getUniqueName(method);
    }

    public void generateStatic(CodeEmitter e, final Context context) {
        /* generates:
           static {
             Class cls = findClass("java.lang.Object");
             METHOD_1 = cls.getDeclaredMethod("toString", new Class[0]);

             Class thisClass = findClass("NameOfThisClass");
             CGLIB$ACCESS_0 = MethodProxy.create(thisClass.getClassLoader(),
                                                 cls, "toString()Ljava.lang.String;",
                                                 thisClass, "CGLIB$ACCESS_0()Ljava.lang.String;");
           }
        */

        Local thisclass = e.make_local();
        ComplexOps.load_class_this(e);
        e.dup();
        e.store_local(thisclass);
        e.invoke_virtual(Constants.TYPE_CLASS, GET_CLASS_LOADER);
        
        for (Iterator it = context.getMethods(); it.hasNext();) {
            e.dup();
            Method method = (Method)it.next();
            ReflectOps.load_method(e, method);
            e.dup();
            e.putfield(getFieldName(context, method));

            String accessName = getAccessName(context, method);
            Signature sig = ReflectUtils.getSignature(method);
            e.invoke_virtual(METHOD, GET_DECLARING_CLASS);
            e.push(method.getName() + sig.getDescriptor());

            e.load_local(thisclass);
            e.push(accessName + sig.getDescriptor());
            e.invoke_static(METHOD_PROXY, MAKE_PROXY);
            e.putfield(accessName);
        }
    }
}
