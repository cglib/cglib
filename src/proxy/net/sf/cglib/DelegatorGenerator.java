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
import java.beans.*;
import java.util.*;

/**
 * @version $Id: DelegatorGenerator.java,v 1.11 2003/06/01 00:00:36 herbyderby Exp $
 */
class DelegatorGenerator extends CodeGenerator {
    private static final String FIELD_NAME = "CGLIB$DELEGATES";
    private static final Method NEW_INSTANCE =
      ReflectUtils.findMethod("Delegator$Factory.cglib_newInstance(Object[])");

    private Class[] classes;
    private boolean multicast;
    private boolean bean;
        
    public DelegatorGenerator(Class cls, boolean multicast, String className, Class[] classes, ClassLoader loader, boolean bean) {
        super(className, cls, loader);
        this.multicast = multicast;
        this.classes = classes;
        this.bean = bean;
    }

    protected void generate() throws NoSuchMethodException {
        declare_interface(Delegator.Factory.TYPE);

        Map methodMap = new HashMap();
        for (int i = 0; i < classes.length; i++) {
            Class clazz = classes[i];
            Method[] methods;
            if (bean) {
                methods = getBeanMethods(clazz);
            } else {
                if (!clazz.isInterface()) {
                    throw new IllegalArgumentException(clazz + " is not an interface");
                }
                declare_interface(clazz);
                methods = clazz.getMethods();
            }
            for (int j = 0; j < methods.length; j++) {
                Method method = methods[j];
                Object key = MethodWrapper.create(method);
                List list = (List)methodMap.get(key);
                if (list == null) {
                    list = new LinkedList();
                    methodMap.put(key, list);
                }
                if (multicast || list.size() == 0) {
                    list.add(new MethodInfo(method, i));
                }
            }
        }
        generateConstructor();
        generateFactoryMethod(NEW_INSTANCE);
        for (Iterator it = methodMap.values().iterator(); it.hasNext();) {
            generateProxy((List)it.next());
        }
    }

    private static class MethodInfo
    {
        private Method method;
        private int arrayref;
        
        public MethodInfo(Method method, int arrayref) {
            this.method = method;
            this.arrayref = arrayref;
        }
        
        public Method getMethod() {
            return method;
        }

        public int getArrayRef() {
            return arrayref;
        }
    }

    private static Method[] getBeanMethods(Class clazz) {
        try {
            BeanInfo info = Introspector.getBeanInfo(clazz, Object.class);
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
            List methods = new ArrayList(descriptors.length * 2);
            for (int i = 0; i < descriptors.length; i++) {
                PropertyDescriptor pd = descriptors[i];
                Method read = pd.getReadMethod();
                if (read != null) {
                    methods.add(read);
                }
                Method write = pd.getWriteMethod();
                if (write != null) {
                    methods.add(write);
                }
            }
            return (Method[])methods.toArray(new Method[methods.size()]);
        } catch (IntrospectionException e) {
            throw new CodeGenerationException(e);
        }
    }

    private void generateConstructor() {
        declare_field(Modifier.PRIVATE, Object[].class, FIELD_NAME);
        begin_constructor(Constants.TYPES_OBJECT_ARRAY);
        load_this();
        super_invoke_constructor();
        load_this();
        load_arg(0);
        putfield(FIELD_NAME);
        return_value();
        end_method();
    }

    private void generateProxy(List methods) {
        begin_method(((MethodInfo)methods.get(0)).getMethod());
        for (Iterator it = methods.iterator(); it.hasNext();) {
            MethodInfo info = (MethodInfo)it.next();
            load_this();
            getfield(FIELD_NAME);
            aaload(info.getArrayRef());
            checkcast(info.getMethod().getDeclaringClass());
            load_args();
            invoke(info.getMethod());
        }
        return_value();
        end_method();
    }
}
