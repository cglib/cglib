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
package net.sf.cglib.reflect;

import net.sf.cglib.core.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

abstract public class FastClass
{
    private Class type;

    protected FastClass() {
        // track the bug down
        Error e = new Error("Using the FastClass empty constructor--please report to the cglib-devel mailing list");
        e.printStackTrace();
        throw e;
    }

    protected FastClass(Class type) {
        this.type = type;
    }

    public static FastClass create(Class type) {
    
        return create(type.getClassLoader(),type);
        
    }
    public static FastClass create(ClassLoader loader, Class type) {
        Generator gen = new Generator();
        gen.setType(type);
        gen.setClassLoader(loader);
        return gen.create();
    }

    public static class Generator extends AbstractClassGenerator
    {
        private static final Source SOURCE = new Source(FastClass.class.getName());
        private Class type;
        
        public Generator() {
            super(SOURCE);
        }

        public void setType(Class type) {
            this.type = type;
        }
        
        public FastClass create() {
            setNamePrefix(type.getName());
            return (FastClass)super.create(type.getName());
        }

        protected ClassLoader getDefaultClassLoader() {
            return type.getClassLoader();
        }

        public void generateClass(ClassVisitor v) throws Exception {
            new FastClassEmitter(v, getClassName(), type);
        }

        protected Object firstInstance(Class type) {
            return ReflectUtils.newInstance(type,
                                            new Class[]{ Class.class },
                                            new Object[]{ this.type });
        }

        protected Object nextInstance(Object instance) {
            return instance;
        }
    }
    
    public Object invoke(String name, Class[] parameterTypes, Object obj, Object[] args) throws InvocationTargetException {
        return invoke(getIndex(name, parameterTypes), obj, args);
    }

    public Object newInstance() throws InvocationTargetException {
        return newInstance(getIndex(Constants.EMPTY_CLASS_ARRAY), null);
    }

    public Object newInstance(Class[] parameterTypes, Object[] args) throws InvocationTargetException {
        return newInstance(getIndex(parameterTypes), args);
    }
    
    public FastMethod getMethod(Method method) {
        return new FastMethod(this, method);
    }

    public FastConstructor getConstructor(Constructor constructor) {
        return new FastConstructor(this, constructor);
    }

    public FastMethod getMethod(String name, Class[] parameterTypes) {
        try {
            return getMethod(type.getMethod(name, parameterTypes));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    public FastConstructor getConstructor(Class[] parameterTypes) {
        try {
            return getConstructor(type.getConstructor(parameterTypes));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    public String getName() {
        return type.getName();
    }

    public Class getJavaClass() {
        return type;
    }

    public String toString() {
        return type.toString();
    }

    public int hashCode() {
        return type.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof FastClass)) {
            return false;
        }
        return type.equals(((FastClass)o).type);
    }

    /**
     * Return the index of the matching method. The index may be used
     * later to invoke the method with less overhead. If more than one
     * method matches (i.e. they differ by return type only), one is
     * chosen arbitrarily.
     * @see #invoke(int, Object, Object[])
     * @param name the method name
     * @param parameterTypes the parameter array
     * @return the index, or <code>-1</code> if none is found.
     */
    abstract public int getIndex(String name, Class[] parameterTypes);

    /**
     * Return the index of the matching constructor. The index may be used
     * later to create a new instance with less overhead.
     * @see #newInstance(int, Object[])
     * @param parameterTypes the parameter array
     * @return the constructor index, or <code>-1</code> if none is found.
     */
    abstract public int getIndex(Class[] parameterTypes);

    /**
     * Invoke the method with the specified index.
     * @see getIndex(name, Class[])
     * @param index the method index
     * @param obj the object the underlying method is invoked from
     * @param args the arguments used for the method call
     * @throws java.lang.reflect.InvocationTargetException if the underlying method throws an exception
     */
    abstract public Object invoke(int index, Object obj, Object[] args) throws InvocationTargetException;

    /**
     * Create a new instance using the specified constructor index and arguments.
     * @see getIndex(Class[])
     * @param index the constructor index
     * @param args the arguments passed to the constructor
     * @throws java.lang.reflect.InvocationTargetException if the constructor throws an exception
     */
    abstract public Object newInstance(int index, Object[] args) throws InvocationTargetException;

    abstract public int getIndex(Signature sig);

    /**
     * Returns the maximum method index for this class.
     */
    abstract public int getMaxIndex();

    protected static String getSignatureWithoutReturnType(String name, Class[] parameterTypes) {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append('(');
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(Type.getDescriptor(parameterTypes[i]));
        }
        sb.append(')');
        return sb.toString();
    }
}
