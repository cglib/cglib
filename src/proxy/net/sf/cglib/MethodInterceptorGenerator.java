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

class MethodInterceptorGenerator
implements CallbackGenerator
{
    public static final MethodInterceptorGenerator INSTANCE = new MethodInterceptorGenerator();

     private static final Method MAKE_PROXY =
       ReflectUtils.findMethod("MethodProxy.create(Class, String, Class, String)");
    private static final Method AROUND_ADVICE =
      ReflectUtils.findMethod("MethodInterceptor.intercept(Object, Method, Object[], MethodProxy)");

    public void generate(Emitter cg, Context context) {
        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();
            String accessName = getAccessName(context, method);
            String fieldName = getFieldName(context, method);

            cg.declare_field(Constants.PRIVATE_FINAL_STATIC, Method.class, fieldName);
            cg.declare_field(Constants.PRIVATE_FINAL_STATIC, MethodProxy.class, accessName);
            generateAccessMethod(cg, context, method);
            generateAroundMethod(cg, context, method);
        }
    }

    private String getFieldName(Context context, Method method) {
        return "CGLIB$$METHOD_" + context.getUniqueName(method);
    }
    
    private String getAccessName(Context context, Method method) {
        return "CGLIB$$ACCESS_" + context.getUniqueName(method);
    }

    private void generateAccessMethod(Emitter cg, Context context, Method method) {
        cg.begin_method(Modifier.FINAL,
                        method.getReturnType(),
                        getAccessName(context, method),
                        method.getParameterTypes(),
                        method.getExceptionTypes());
        if (Modifier.isAbstract(method.getModifiers())) {
            Virt.throw_exception(cg, AbstractMethodError.class, method.toString() + " is abstract" );
        } else {
            cg.load_this();
            cg.load_args();
            cg.super_invoke(method);
        }
        cg.return_value();
        cg.end_method();
    }

    private void generateAroundMethod(Emitter cg,
                                      Context context,
                                      Method method) {
        cg.begin_method(method, context.getModifiers(method));
        Label nullInterceptor = cg.make_label();
        context.emitCallback();
        cg.dup();
        cg.ifnull(nullInterceptor);

        cg.load_this();
        cg.getfield(getFieldName(context, method));
        Virt.create_arg_array(cg);
        cg.getfield(getAccessName(context, method));
        cg.invoke(AROUND_ADVICE);
        Virt.unbox_or_zero(cg, method.getReturnType());
        cg.return_value();

        cg.mark(nullInterceptor);
        cg.load_this();
        cg.load_args();
        cg.super_invoke(method);
        cg.return_value();
        cg.end_method();
    }

    public void generateStatic(Emitter cg, Context context) {
        /* generates:
           static {
             Class [] args;
             Class cls = findClass("java.lang.Object");
             args = new Class[0];
             METHOD_1 = cls.getDeclaredMethod("toString", args);

             Class thisClass = findClass("NameOfThisClass");
             CGLIB$ACCESS_0 = MethodProxy.create(cls, "toString()Ljava.lang.String;",
                                                 thisClass, "CGLIB$ACCESS_0()Ljava.lang.String;");
           }
        */

        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();
            Virt.load_method(cg, method);
            cg.dup();
            cg.putfield(getFieldName(context, method));

            String accessName = getAccessName(context, method);
            String desc = ReflectUtils.getMethodDescriptor(method);
            cg.invoke(MethodConstants.GET_DECLARING_CLASS);
            cg.push(method.getName() + desc);
            Virt.load_class_this(cg);
            cg.push(accessName + desc);
            cg.invoke(MAKE_PROXY);
            cg.putfield(accessName);
        }
    }
}
