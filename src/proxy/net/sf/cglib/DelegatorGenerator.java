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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/* package */ class DelegatorGenerator extends CodeGenerator implements ClassFileConstants {
    private static final String FIELD_NAME = "CGLIB$DELEGATES";

    private Class[] interfaces;
        
    /* package */ DelegatorGenerator(String className, Class[] interfaces, ClassLoader loader) {
        super(className, Object.class, loader);
        this.interfaces = interfaces;
    }

    protected void generate() throws NoSuchMethodException {
        declare_interfaces(interfaces);
        declare_interface(Delegator.Factory.class);
        generateConstructor();
        generateFactory();

        Set methodSet = new HashSet();
        for (int i = 0; i < interfaces.length; i++) {
            Class iface = interfaces[i];
            if (!iface.isInterface()) {
                throw new IllegalArgumentException(iface + " is not an interface");
            }
            Method[] methods = iface.getMethods();
            for (int j = 0; j < methods.length; j++) {
                Method method = methods[j];
                Object methodKey = MethodWrapper.newInstance(method);
                if (!methodSet.contains(methodKey)) {
                    methodSet.add(methodKey);
                    generateProxy(iface, method, i);
                }
            }
        }
    }

    private void generateConstructor() {
        declare_field(Modifier.PRIVATE, OBJECT_ARRAY_CLASS, FIELD_NAME);
        begin_constructor(OBJECT_ARRAY_CLASS_ARRAY);
        load_this();
        super_invoke_constructor();
        load_this();
        load_arg(0);
        putfield(FIELD_NAME);
        return_value();
        end_constructor();
    }

    private void generateFactory() throws NoSuchMethodException {
        Method newInstance =
            Delegator.Factory.class.getMethod("cglib_newInstance", OBJECT_ARRAY_CLASS_ARRAY);
        begin_method(newInstance);
        new_instance_this();
        dup();
        load_arg(0);
        invoke_constructor_this(OBJECT_ARRAY_CLASS_ARRAY);
        return_value();
        end_method();
    }

    private void generateProxy(Class iface, Method method, int arrayref) {
        begin_method(method);
        load_this();
        getfield(FIELD_NAME);
        aaload(arrayref);
        checkcast(iface);
        load_args();
        invoke(method);
        return_value();
        end_method();
    }
}
