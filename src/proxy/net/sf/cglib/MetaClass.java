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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import net.sf.cglib.util.*;

/**
 * @author Juozas Baliuka
 */
abstract public class MetaClass  {
    private static final FactoryCache cache = new FactoryCache(MetaClass.class);
    private static final Constructor GENERATOR =
      ReflectUtils.findConstructor("MetaClassGenerator(Class, String[], String[], Class[])");
    private static final MetaClassKey KEY_FACTORY =
      (MetaClassKey)KeyFactory.create(MetaClassKey.class, null);
    private static final MemberKey MEMBER_KEY_FACTORY =
      (MemberKey)KeyFactory.create(MemberKey.class, null);
    
    protected Class target;
    protected String[] getters, setters;
    protected Class[] types;
    protected Map members = new HashMap();
    
    interface MetaClassKey {
        public Object newInstance(Class target, String[] getters, String[] setters, Class[] types);
    }
    
    interface MemberKey {
        public Object newInstance(Class target, String name, Class[] types);
    }

    protected MetaClass() { }

    abstract public Object newInstance();
    abstract public Object[] getPropertyValues(Object bean);
    abstract public void setPropertyValues(Object bean, Object[] values);
        
    public Class[] getPropertyTypes() {
        return (Class[])types.clone();
    }
    
    private void addMembers() {
        Method[] methods = target.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            members.put(methodKey(m.getName(), m.getParameterTypes()), MethodProxy.create(m, m));
        }
        
        Constructor[] constructors = target.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor c = constructors[i];
            members.put(cstructKey(c.getParameterTypes()), ConstructorProxy.create(c));
        }
    }

    public MethodProxy getMethod(String name, Class[] types) {
        return (MethodProxy)members.get(methodKey(name, types));
    }
    
    public ConstructorProxy getConstructor(Class[] types) {
        return (ConstructorProxy)members.get(cstructKey(types));
    }

    private Object methodKey(String methodName, Class[] parameterTypes) {
        return MEMBER_KEY_FACTORY.newInstance(target, methodName, parameterTypes);
    }

    private Object cstructKey(Class[] parameterTypes) {
        return MEMBER_KEY_FACTORY.newInstance(target, "<init>", parameterTypes);
    }
    
    public String[] getGetters() {
        return (String[])getters.clone();
    }
    
    public String[] getSetters() {
        return (String[])setters.clone();
    }
        
    public static MetaClass getInstance(ClassLoader loader,
                                        Class target,
                                        String[] getters,
                                        String[] setters,
                                        Class[] types) {
        synchronized (cache) {
            MetaClass singleton =
                (MetaClass)cache.getFactory(loader,
                                            KEY_FACTORY.newInstance(target, getters, setters, types),
                                            GENERATOR,
                                            target,
                                            getters,
                                            setters,
                                            types);
            if (singleton.target == null) {
                singleton.target = target;

                int length = getters.length;
                singleton.getters = new String[length];
                System.arraycopy(getters, 0, singleton.getters, 0, length);

                singleton.setters = new String[length];
                System.arraycopy(setters, 0, singleton.setters, 0, length);
                                               
                singleton.types = new Class[types.length];
                System.arraycopy(types, 0, singleton.types, 0, types.length);

                singleton.addMembers();
            }
            return singleton;
        }
    }
}
