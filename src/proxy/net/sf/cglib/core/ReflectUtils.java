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
package net.sf.cglib.core;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import org.objectweb.asm.Type;

/**
 * @version $Id: ReflectUtils.java,v 1.10 2003/09/22 01:02:11 herbyderby Exp $
 */
public class ReflectUtils {
    private ReflectUtils() { }
    
    private static final Map primitives = new HashMap(8);
    private static final Map transforms = new HashMap(8);
    private static final ClassLoader defaultLoader = ReflectUtils.class.getClassLoader();
    
    private static final String[] CGLIB_PACKAGES = {
        "java.lang",
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
    }

    public static Type[] getExceptionTypes(Member member) {
        if (member instanceof Method) {
            return TypeUtils.getTypes(((Method)member).getExceptionTypes());
        } else if (member instanceof Constructor) {
            return TypeUtils.getTypes(((Constructor)member).getExceptionTypes());
        } else {
            throw new IllegalArgumentException("Cannot get exception types of a field");
        }
    }

    public static Signature getSignature(Member member) {
        if (member instanceof Method) {
            return new Signature(member.getName(), Type.getMethodDescriptor((Method)member));
        } else if (member instanceof Constructor) {
            Type[] types = TypeUtils.getTypes(((Constructor)member).getParameterTypes());
            return new Signature(Constants.CONSTRUCTOR_NAME,
                                 Type.getMethodDescriptor(Type.VOID_TYPE, types));
                                                          
        } else {
            throw new IllegalArgumentException("Cannot get signature of a field");
        }
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


    public static Object newInstance(Class type) {
        return newInstance(type, Constants.EMPTY_CLASS_ARRAY, null);
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

//     public static Class[] getClasses(Object[] objects) {
//         Class[] classes = new Class[objects.length];
//         for (int i = 0; i < objects.length; i++) {
//             classes[i] = objects[i].getClass();
//         }
//         return classes;
//     }

    public static Method findNewInstance(Class iface) {
        Method m = findInterfaceMethod(iface);
        if (!m.getName().equals("newInstance")) {
            throw new IllegalArgumentException(iface + " missing newInstance method");
        }
        return m;
    }

//     public static PropertyDescriptor[] getBeanProperties(Class type) {
//         return getPropertiesHelper(type, true, true);
//     }

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

    public static List addAllMethods(Class type, List list) {
        list.addAll(java.util.Arrays.asList(type.getDeclaredMethods()));
        Class superclass = type.getSuperclass();
        if (superclass != null) {
            addAllMethods(superclass, list);
        }
        Class[] interfaces = type.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            addAllMethods(interfaces[i], list);
        }
        return list;
    }

    public static Method findInterfaceMethod(Class iface) {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }
        Method[] methods = iface.getDeclaredMethods();
        if (methods.length != 1) {
            throw new IllegalArgumentException("expecting exactly 1 method in " + iface);
        }
        return methods[0];
    }
}
