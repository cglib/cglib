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
package net.sf.cglib.util;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * @version $Id: ReflectUtils.java,v 1.10 2003/09/10 17:49:10 herbyderby Exp $
 */
public class ReflectUtils {
    private ReflectUtils() { }
    
    private static final Map primitives = new HashMap(8);
    private static final Map transforms = new HashMap(8);
    private static final Map primitiveToWrapper = new HashMap();
    private static final Map wrapperToPrimitive = new HashMap();
    private static final ClassLoader defaultLoader = ReflectUtils.class.getClassLoader();
    private static final String[] CGLIB_PACKAGES = {
        "java.lang",
        "java.lang.reflect",
        "net.sf.cglib",
        "net.sf.cglib.beans",
        "net.sf.cglib.reflect",
        "net.sf.cglib.delegates",
        "net.sf.cglib.algorithm"
    };
    static {
        primitives.put("byte", Byte.TYPE);
        primitives.put("char", Character.TYPE);
        primitives.put("double", Double.TYPE);
        primitives.put("float", Float.TYPE);
        primitives.put("int", Integer.TYPE);
        primitives.put("long", Long.TYPE);
        primitives.put("short", Short.TYPE);
        primitives.put("boolean", Boolean.TYPE);

        transforms.put("byte", "B");
        transforms.put("char", "C");
        transforms.put("double", "D");
        transforms.put("float", "F");
        transforms.put("int", "I");
        transforms.put("long", "J");
        transforms.put("short", "S");
        transforms.put("boolean", "Z");

        primitiveToWrapper.put(Boolean.TYPE, Boolean.class);
        primitiveToWrapper.put(Character.TYPE, Character.class);
        primitiveToWrapper.put(Long.TYPE, Long.class);
        primitiveToWrapper.put(Double.TYPE, Double.class);
        primitiveToWrapper.put(Float.TYPE, Float.class);
        primitiveToWrapper.put(Short.TYPE, Short.class);
        primitiveToWrapper.put(Integer.TYPE, Integer.class);
        primitiveToWrapper.put(Byte.TYPE, Byte.class);

        wrapperToPrimitive.put(Boolean.class, Boolean.TYPE);
        wrapperToPrimitive.put(Character.class, Character.TYPE);
        wrapperToPrimitive.put(Long.class, Long.TYPE);
        wrapperToPrimitive.put(Double.class, Double.TYPE);
        wrapperToPrimitive.put(Float.class, Float.TYPE);
        wrapperToPrimitive.put(Short.class, Short.TYPE);
        wrapperToPrimitive.put(Integer.class, Integer.TYPE);
        wrapperToPrimitive.put(Byte.class, Byte.TYPE);
    }

    public static Class getBoxedType(Class type) {
        Class boxed = (Class)primitiveToWrapper.get(type);
        return (boxed != null) ? boxed : type;
    }

    public static Class getUnboxedType(Class type) {
        Class unboxed = (Class)wrapperToPrimitive.get(type);
        return (unboxed != null) ? unboxed : type;
    }
    
    public static Constructor findConstructor(String desc) {
        return findConstructor(desc, defaultLoader);
    }

    public static Constructor findConstructor(String desc, ClassLoader loader) {
        try {
            int lparen = desc.indexOf('(');
            String className = desc.substring(0, lparen).trim();
            return getClass(className, loader).getConstructor(parseTypes(desc, loader));
        } catch (ClassNotFoundException e) {
            throw new CodeGenerationException(e);
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }

    public static Method findMethod(String desc) {
        return findMethod(desc, defaultLoader); 
    }

    public static Method findMethod(String desc, ClassLoader loader) {
        try {
            int lparen = desc.indexOf('(');
            int dot = desc.lastIndexOf('.', lparen);
            String className = desc.substring(0, dot).trim();
            String methodName = desc.substring(dot + 1, lparen).trim();
            return getClass(className, loader).getDeclaredMethod(methodName, parseTypes(desc, loader));
        } catch (ClassNotFoundException e) {
            throw new CodeGenerationException(e);
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }

    private static Class[] parseTypes(String desc, ClassLoader loader) throws ClassNotFoundException {
        int lparen = desc.indexOf('(');
        int rparen = desc.indexOf(')', lparen);
        List params = new ArrayList();
        int start = lparen + 1;
        for (;;) {
            int comma = desc.indexOf(',', start);
            if (comma < 0) {
                break;
            }
            params.add(desc.substring(start, comma).trim());
            start = comma + 1;
        }
        if (start < rparen) {
            params.add(desc.substring(start, rparen).trim());
        }
        Class[] types = new Class[params.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = getClass((String)params.get(i), loader);
        }
        return types;
    }

    private static Class getClass(String className, ClassLoader loader) throws ClassNotFoundException {
        return getClass(className, loader, CGLIB_PACKAGES);
    }
    
    private static Class getClass(String className, ClassLoader loader, String[] packages) throws ClassNotFoundException {
        String save = className;
        int dimensions = 0;
        int index = 0;
        while ((index = className.indexOf("[]", index) + 1) > 0) {
            dimensions++;
        }
        StringBuffer brackets = new StringBuffer(className.length() - dimensions);
        for (int i = 0; i < dimensions; i++) {
            brackets.append('[');
        }
        className = className.substring(0, className.length() - 2 * dimensions);

        String prefix = (dimensions > 0) ? brackets + "L" : "";
        String suffix = (dimensions > 0) ? ";" : "";
        try {
            return Class.forName(prefix + className + suffix, false, loader);
        } catch (ClassNotFoundException ignore) { }
        for (int i = 0; i < packages.length; i++) {
            try {
                return Class.forName(prefix + packages[i] + '.' + className + suffix, false, loader);
            } catch (ClassNotFoundException ignore) { }
        }
        if (dimensions == 0) {
            Class c = (Class)primitives.get(className);
            if (c != null) {
                return c;
            }
        } else {
            String transform = (String)transforms.get(className);
            if (transform != null) {
                try {
                    return Class.forName(brackets + transform, false, loader); 
                } catch (ClassNotFoundException ignore) { }
            }
        }
        throw new ClassNotFoundException(save);
    }


    public static Class forName(String name, ClassLoader loader) {
        try {
            return Class.forName(name, false, loader);
        } catch (ClassNotFoundException e) {
            throw new CodeGenerationException(e);
        }
    }

    public static Object newInstance(Class type) {
        return newInstance(type, Constants.TYPES_EMPTY, null);
    }

    public static Object newInstance(Class type, Class[] parameterTypes, Object[] args) {
        return newInstance(getConstructor(type, parameterTypes), args);
    }

    public static Object newInstance(Constructor cstruct, Object[] args) {
        boolean flag = cstruct.isAccessible();
        try {
            cstruct.setAccessible(true);
            Object result = cstruct.newInstance(args);
            return result;
        } catch (InstantiationException e) {
            throw new CodeGenerationException(e);
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException(e);
        } catch (InvocationTargetException e) {
            throw new CodeGenerationException(e.getTargetException());
        } finally {
            cstruct.setAccessible(flag);
        }
    }

    public static Constructor getConstructor(Class type, Class[] parameterTypes) {
        try {
            return type.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }

    public static Class[] getClasses(Object[] objects) {
        Class[] classes = new Class[objects.length];
        for (int i = 0; i < objects.length; i++) {
            classes[i] = objects[i].getClass();
        }
        return classes;
    }

    public static Method findNewInstance(Class iface) {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }
        Method newInstance = null;
        Method[] methods = iface.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("newInstance")) {
                if (newInstance != null) {
                    throw new IllegalArgumentException("Multiple newInstance methods");
                }
                newInstance = methods[i];
            }
        }
        if (newInstance == null) {
            throw new IllegalArgumentException("Missing newInstance method");
        }
        return newInstance;
    }

    // getPackage returns null on JDK 1.2
    public static String getPackageName(Class type) {
        String name = type.getName();
        int idx = name.lastIndexOf('.');
        return (idx < 0) ? "" : name.substring(0, idx);
    }

    public static String getNameWithoutPackage(Class type) {
        String pkg = getPackageName(type);
        int len = pkg.length();
        return (len == 0) ? type.getName() : type.getName().substring(len + 1);
    }

    public static PropertyDescriptor[] getBeanProperties(Class type) {
        return getPropertiesHelper(type, true, true);
    }

    public static PropertyDescriptor[] getBeanGetters(Class type) {
        return getPropertiesHelper(type, true, false);
    }

    public static PropertyDescriptor[] getBeanSetters(Class type) {
        return getPropertiesHelper(type, false, true);
    }

    private static PropertyDescriptor[] getPropertiesHelper(Class type, boolean read, boolean write) {
        try {
            BeanInfo info = Introspector.getBeanInfo(type, Object.class);
            PropertyDescriptor[] all = info.getPropertyDescriptors();
            if (read && write) {
                return all;
            }
            List properties = new ArrayList(all.length);
            for (int i = 0; i < all.length; i++) {
                PropertyDescriptor pd = all[i];
                if ((read && pd.getReadMethod() != null) ||
                    (write && pd.getWriteMethod() != null)) {
                    properties.add(pd);
                }
            }
            return (PropertyDescriptor[])properties.toArray(new PropertyDescriptor[properties.size()]);
        } catch (IntrospectionException e) {
            throw new CodeGenerationException(e);
        }
    }

    public static Method[] getPropertyMethods(PropertyDescriptor[] properties, boolean read, boolean write) {
        Set methods = new HashSet();
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor pd = properties[i];
            if (read) {
                methods.add(pd.getReadMethod());
            }
            if (write) {
                methods.add(pd.getWriteMethod());
            }
        }
        methods.remove(null);
        return (Method[])methods.toArray(new Method[methods.size()]);
    }

    public static boolean arrayEquals(Object[] a1, Object[] a2) {
        if ((a1 == null) ^ (a2 == null)) {
            return false;
        }
        if (a1.length != a2.length) {
            return false;
        }
        for (int i = 0; i < a1.length; i++) {
            Object o1 = a1[i];
            Object o2 = a2[i];
            if (o1 == null) {
                if (o2 != null) {
                    return false;
                }
            } else if (o2 == null) {
                return false;
            } else {
                Class c1 = o1.getClass();
                Class c2 = o2.getClass();
                if (!c1.equals(c2)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Method findDeclaredMethod(Class type, String methodName, Class[] parameterTypes)
    throws NoSuchMethodException {
        Class cl = type;
        while (cl != null) {
            try {
                return cl.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                cl = cl.getSuperclass();
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    public static String getMethodDescriptor(Method method) {
        return getMethodDescriptor(method.getReturnType(), method.getParameterTypes());
    }
    
    public static String getMethodDescriptor(Class returnType, Class[] parameterTypes) {
        StringBuffer buf = new StringBuffer();
        buf.append('(');
        if (parameterTypes != null) {
            for (int i = 0; i < parameterTypes.length; ++i) {
                getDescriptor(buf, parameterTypes[i]);
            }
        }
        buf.append(')');
        getDescriptor(buf, returnType);
        return buf.toString();
    }

    public static String getDescriptor(Class type) {
        StringBuffer buf = new StringBuffer();
        getDescriptor(buf, type);
        return buf.toString();
    }

    // adapted from ASM Type class
    public static void getDescriptor(final StringBuffer buf, final Class c) {
        Class d = c;
        while (true) {
            if (d.isPrimitive()) {
                char car;
                if (d == Integer.TYPE) {
                    car = 'I';
                } else if (d == Void.TYPE) {
                    car = 'V';
                } else if (d == Boolean.TYPE) {
                    car = 'Z';
                } else if (d == Byte.TYPE) {
                    car = 'B';
                } else if (d == Character.TYPE) {
                    car = 'C';
                } else if (d == Short.TYPE) {
                    car = 'S';
                } else if (d == Double.TYPE) {
                    car = 'D';
                } else if (d == Float.TYPE) {
                    car = 'F';
                } else /*if (d == Long.TYPE)*/ {
                    car = 'J';
                }
                buf.append(car);
                return;
            } else if (d.isArray()) {
                buf.append('[');
                d = d.getComponentType();
            } else {
                buf.append('L');
                buf.append(d.getName());
                buf.append(';');
                return;
            }
        }
    }

    public static Object[] filter(Object[] a, Predicate p) {
        List c = new ArrayList(Arrays.asList(a));
        filter(c, p);
        return c.toArray((Object[])Array.newInstance(a.getClass().getComponentType(), c.size()));
    }

    public static void filter(Collection c, Predicate p) {
        Iterator it = c.iterator();
        while (it.hasNext()) {
            if (!p.evaluate(it.next())) {
                it.remove();
            }
        }
    }
}
