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

// TODO: don't require exact match for return type

/**
 * <b>DOCUMENTATION FROM APACHE AVALON DELEGATE CLASS</b>
 *
 * <p>
 * Delegates are a typesafe pointer to another method.  Since Java does not
 * have language support for such a construct, this utility will construct
 * a proxy that forwards method calls to any method with the same signature.
 * This utility is inspired in part by the C# delegate mechanism.  We
 * implemented it in a Java-centric manner.
 * </p>
 *
 * <h2>Delegate</h2>
 * <p>
 *   Any interface with one method can become the interface for a delegate.
 *   Consider the example below:
 * </p>
 *
 * <pre>
 *   public interface MainDelegate {
 *       int main(String[] args);
 *   }
 * </pre>
 *
 * <p>
 *   The interface above is an example of an interface that can become a
 *   delegate.  It has only one method, and the interface is public.  In
 *   order to create a delegate for that method, all we have to do is
 *   call <code>MethodDelegate.create(this, "alternateMain", MainDelegate.class)</code>.
 *   The following program will show how to use it:
 * </p>
 *
 * <pre>
 *   public class Main {
 *       public static int main( String[] args ) {
 *           Main newMain = new Main();
 *           MainDelegate start = (MainDelegate)
 *               MethodDelegate.create(newMain, "alternateMain", MainDelegate.class);
 *           return start.main( args );
 *       }
 *
 *       public int alternateMain( String[] args ) {
 *           for (int i = 0; i < args.length; i++) {
 *               System.out.println( args[i] );
 *           }
 *           return args.length;
 *       }
 *   }
 * </pre>
 *
 * <p>
 *   By themselves, delegates don't do much.  Their true power lies in the fact that
 *   they can be treated like objects, and passed to other methods.  In fact that is
 *   one of the key building blocks of building Intelligent Agents which in tern are
 *   the foundation of artificial intelligence.  In the above program, we could have
 *   easily created the delegate to match the static <code>main</code> method by
 *   substituting the delegate creation call with this:
 *   <code>MethodDelegate.createStatic(getClass(), "main", MainDelegate.class)</code>.
 * </p>
 * <p>
 *   Another key use for Delegates is to register event listeners.  It is much easier
 *   to have all the code for your events separated out into methods instead of individual
 *   classes.  One of the ways Java gets around that is to create anonymous classes.
 *   They are particularly troublesome because many Debuggers do not know what to do
 *   with them.  Anonymous classes tend to duplicate alot of code as well.  We can
 *   use any interface with one declared method to forward events to any method that
 *   matches the signature (although the method name can be different).
 * </p>
 *
 * <h3>Equality</h3>
 *  The criteria that we use to test if two delegates are equal are:
 *   <ul>
 *     <li>
 *       They both refer to the same instance.  That is, the <code>instance</code>
 *       parameter passed to the newDelegate method was the same for both. The
 *       instances are compared with the identity equality operator, <code>==</code>.
 *     </li>
 *     <li>They refer to the same method as resolved by <code>Method.equals</code>.</li>
 *   </ul>
 *
 * @version $Id: MethodDelegate.java,v 1.8 2003/06/01 00:00:35 herbyderby Exp $
 */
abstract public class MethodDelegate {
     static final Class TYPE = MethodDelegate.class;
    private static final FactoryCache cache = new FactoryCache();
    private static final ClassLoader defaultLoader = TYPE.getClassLoader();
    private static final ClassNameFactory nameFactory = new ClassNameFactory("DelegatedByCGLIB");

    private static final MethodDelegateKey keyFactory =
      (MethodDelegateKey)KeyFactory.create(MethodDelegateKey.class, null);

    private static final Method NEW_INSTANCE =
      ReflectUtils.findMethod("MethodDelegate.cglib_newInstance(Object)");

    
     interface MethodDelegateKey {
        public Object newInstance(Class delegateClass, String methodName, Class iface);
    }

    protected Object delegate;
    protected String eqMethod;

    public boolean equals(Object obj) {
        MethodDelegate other = (MethodDelegate)obj;
        return delegate == other.delegate && eqMethod.equals(other.eqMethod);
    }

    public int hashCode() {
        return delegate.hashCode() ^ eqMethod.hashCode();
    }

    public Object getInvocationTarget() {
        return delegate;
    }

    protected MethodDelegate() {
    }

    abstract protected MethodDelegate cglib_newInstance(Object delegate);

    public static MethodDelegate createStatic(Class clazz, String methodName, Class iface) {
        return createHelper(null, clazz, methodName, iface, null);
    }

    public static MethodDelegate createStatic(Class clazz, String methodName, Class iface, ClassLoader loader) {
        return createHelper(null, clazz, methodName, iface, loader);
    }

    public static MethodDelegate create(Object delegate, String methodName, Class iface) {
        return createHelper(delegate, delegate.getClass(), methodName, iface, null);
    }

    public static MethodDelegate create(Object delegate, String methodName, Class iface, ClassLoader loader) {
        return createHelper(delegate, delegate.getClass(), methodName, iface, loader);
    }

    private static MethodDelegate createHelper(Object delegate, Class clazz, String methodName,
                                               Class iface, ClassLoader loader) {
        if (loader == null) {
            loader = defaultLoader;
        }
        Object key = keyFactory.newInstance(clazz, methodName, iface);
        MethodDelegate factory;
        synchronized (cache) {
            factory = (MethodDelegate)cache.get(loader, key);
            if (factory == null) {
                Method method = findProxiedMethod(clazz, methodName, iface);
                String className = nameFactory.getNextName(clazz);
                Class result = new Generator(className, method, iface, loader).define();
                factory = (MethodDelegate)ReflectUtils.newInstance(result);
                cache.put(loader, key, factory);
            }
        }
        return factory.cglib_newInstance(delegate);
    }

    static Method findInterfaceMethod(Class iface) {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }
        Method[] methods = iface.getDeclaredMethods();
        if (methods.length != 1) {
            throw new IllegalArgumentException("expecting exactly 1 method in " + iface);
        }
        return methods[0];
    }

    private static Method findProxiedMethod(Class clazz, String methodName, Class iface) {
        Method proxy = findInterfaceMethod(iface);
        try {
            Method method = clazz.getMethod(methodName, proxy.getParameterTypes());
            if (method == null) {
                throw new IllegalArgumentException("no matching method found");
            }
            if (!proxy.getReturnType().isAssignableFrom(method.getReturnType())) {
                throw new IllegalArgumentException("incompatible return types");
            }
            return method;
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }

    private static class Generator extends CodeGenerator {
        private Method method;
        private Class iface;

        public Generator(String className, Method method, Class iface, ClassLoader loader) {
            super(className, MethodDelegate.class, loader);
            this.method = method;
            this.iface = iface;
        }

        protected void generate() throws NoSuchMethodException, NoSuchFieldException {
            declare_interface(iface);
            declare_field(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC, String.class, "eqMethod");
            generateNullConstructor();

            // generate proxied method
            begin_method(iface.getDeclaredMethods()[0]);
            load_this();
            super_getfield("delegate");
            checkcast(method.getDeclaringClass());
            load_args();
            invoke(method);
            return_value();
            end_method();

            // newInstance
            begin_method(NEW_INSTANCE);
            new_instance_this();
            dup();
            dup2();
            invoke_constructor_this();
            getfield("eqMethod");
            super_putfield("eqMethod");
            load_arg(0);
            super_putfield("delegate");
            return_value();
            end_method();

            // static initializer
            begin_static();
            push(getSignature(method));
            putfield("eqMethod");
            return_value();
            end_method();
        }

        private String getSignature(Method method) {
            StringBuffer sb = new StringBuffer();
            sb.append(method.getDeclaringClass().getName()).append('.');
            sb.append(method.getName()).append('(');
            Class[] types = method.getParameterTypes();
            for (int i = 0; i < types.length; i++) {
                sb.append(types[i].getName());
            }
            return sb.toString();
        }
    }
}
