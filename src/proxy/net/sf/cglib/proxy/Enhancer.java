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

import net.sf.cglib.core.*;
import java.lang.reflect.Method;
import java.util.*;
import org.objectweb.asm.ClassVisitor;

/**
 * Generates dynamic subclasses to enable method interception. This
 * class started as a substitute for the standard Dynamic Proxy support
 * included with JDK 1.3, but one that allowed the proxies to extend a
 * concrete base class, in addition to implementing interfaces. The dynamically
 * generated subclasses override the non-final methods of the superclass and
 * have hooks which callback to user-defined interceptor
 * implementations.
 * <p>
 * The original and most general callback type is the {@link MethodInterceptor}, which
 * in AOP terms enables "around advice"--that is, you can invoke custom code both before
 * and after the invocation of the "super" method. In addition you can modify the
 * arguments before calling the super method, or not call it at all.
 * <p>
 * Although <code>MethodInterceptor</code> is generic enough to meet any
 * interception need, it is often overkill. For simplicity and performance, additional
 * specialized callback types, such as {@link LazyLoader} are also available.
 * Often a single callback type will be used per enhanced class, but you can control
 * which callback is used on a per-method basis with a {@link CallbackFilter}.
 * <p>
 * The most common uses of this class are embodied in the static helper methods. For
 * advanced needs, such as customizing the <code>ClassLoader</code> to use, you should create
 * a new instance of <code>Enhancer</code>. Other classes within CGLIB follow a similar pattern.
 * <p>
 * For an almost drop-in replacement for
 * <code>java.lang.reflect.Proxy</code>, see the {@link Proxy} class.
 */
public class Enhancer extends AbstractClassGenerator
{
    private static final Class[] NULL_CLASS_ARRAY = { null };
    private static final Callback[] NULL_CALLBACK_ARRAY = { null };
    private static final Source SOURCE = new Source(Enhancer.class.getName());
    private static final EnhancerKey KEY_FACTORY =
      (EnhancerKey)KeyFactory.create(EnhancerKey.class, KeyFactory.CLASS_BY_NAME);

    interface EnhancerKey {
        public Object newInstance(Class type,
                                  Class[] interfaces,
                                  CallbackFilter filter,
                                  Class[] callbackTypes,
                                  boolean classOnly,
                                  boolean useFactory);
    }

    private Class[] interfaces;
    private CallbackFilter filter;
    private Callback[] callbacks;
    private Class[] callbackTypes;
    private boolean classOnly;
    private Class superclass;
    private Class[] argumentTypes;
    private Object[] arguments;
    private boolean useFactory = true;

    /**
     * Create a new <code>Enhancer</code>. A new <code>Enhancer</code>
     * object should be used for each generated object, and should not
     * be shared across threads. To create additional instances of a
     * generated class, use the <code>Factory</code> interface.
     * @see Factory
     */
    public Enhancer() {
        super(SOURCE);
    }

    /**
     * Set the class which the generated class will extend. As a convenience,
     * if the supplied superclass is actually an interface, <code>setInterfaces</code>
     * will be called with the appropriate argument instead.
     * Non-interfaces arguments must not be declared as final, and must have an
     * accessible constructor.
     * @param superclass class to extend or interface to implement
     * @see #setInterfaces(Class[])
     */
    public void setSuperclass(Class superclass) {
        if (superclass != null && superclass.isInterface()) {
            setInterfaces(new Class[]{ superclass });
        } else {
            this.superclass = superclass;
        }
    }

    /**
     * Set the interfaces to implement. The <code>Factory</code> interface will
     * always be implemented regardless of what is specified here.
     * @param interfaces array of interfaces to implement, or null
     * @see Factory
     */
    public void setInterfaces(Class[] interfaces) {
        this.interfaces = interfaces;
    }

    /**
     * Set the {@link CallbackFilter} used to map the generated class' methods
     * to a particular callback index.
     * New object instances will always use the same mapping, but may use different
     * actual callback objects.
     * @param filter the callback filter to use when generating a new class
     * @see #setCallbacks
     */
    public void setCallbackFilter(CallbackFilter filter) {
        this.filter = filter;
    }

    /**
     * Set the single {@link Callback} to use. This will override any
     * <code>CallbackFilter</code> that has been specified previously, and
     * ensure that every applicable method is mapped to index zero.
     * Ignored if you use {@link #createClass}.
     * @param callback the callback to use for all methods
     * @see #setCallbackFilter
     * @see #setCallbacks
     */
    public void setCallback(final Callback callback) {
        setCallbacks(new Callback[]{ callback });
        setCallbackFilter(CallbackFilter.ALL_ZERO);
    }

    public void setCallbacks(Callback[] callbacks) {
        if (callbacks == null || callbacks.length == 0) {
            callbacks = NULL_CALLBACK_ARRAY;
        }
        this.callbacks = callbacks;
        callbackTypes = new Class[callbacks.length];
        for (int i = 0; i < callbacks.length; i++) {
            callbackTypes[i] = CallbackUtils.determineType(callbacks[i]);
        }
    }

    public void setUseFactory(boolean useFactory) {
        this.useFactory = useFactory;
    }

    public void setCallbackType(Class callbackType) {
        setCallbackTypes(new Class[]{ callbackType });
        setCallbackFilter(CallbackFilter.ALL_ZERO);
    }

    public void setCallbackTypes(Class[] callbackTypes) {
        if (callbacks != null) {
            throw new IllegalStateException("Cannot call both setCallbacks and setCallbackTypes");
        }
        if (callbackTypes == null || callbackTypes.length == 0) {
            callbackTypes = NULL_CLASS_ARRAY;
        }
        this.callbackTypes = callbackTypes;
    }

    /**
     * Generate a new class if necessary and uses the specified
     * callbacks (if any) to create a new object instance.
     * Uses the no-arg constructor of the superclass.
     * @return a new instance
     */
    public Object create() {
        classOnly = false;
        argumentTypes = null;
        return createHelper();
    }

    /**
     * Generate a new class if necessary and uses the specified
     * callbacks (if any) to create a new object instance.
     * Uses the constructor of the superclass matching the <code>argumentTypes</code>
     * parameter, with the given arguments.
     * @param argumentTypes constructor signature
     * @param arguments compatible wrapped arguments to pass to constructor
     * @return a new instance
     */
    public Object create(Class[] argumentTypes, Object[] arguments) {
        classOnly = false;
        if (argumentTypes == null || arguments == null || argumentTypes.length != arguments.length) {
            throw new IllegalArgumentException("Arguments must be non-null and of equal length");
        }
        this.argumentTypes = argumentTypes;
        this.arguments = arguments;
        return createHelper();
    }

    /**
     * Generate a new class if necessary and return it without creating a new instance.
     * This ignores any callbacks that have been set.
     * To create a new instance you will have to use reflection, and methods
     * called during the constructor will not be intercepted. To avoid this problem,
     * use the multi-arg <code>create</code> method.
     * @see #create(Class[], Object[])
     */
    public Class createClass() {
        classOnly = true;
        return (Class)createHelper();
    }

    private Object createHelper() {
        if (superclass != null) {
            setNamePrefix(superclass.getName());
        } else if (interfaces != null) {
            setNamePrefix(interfaces[ReflectUtils.findPackageProtected(interfaces)].getName());
        }
        if (callbackTypes == null) {
            callbackTypes = NULL_CLASS_ARRAY;
        }
        if (filter == null) {
            filter = CallbackFilter.ALL_ZERO;
        }
        Object key = KEY_FACTORY.newInstance(superclass, interfaces, filter, callbackTypes, classOnly, useFactory);
        return super.create(key);
    }

    protected ClassLoader getDefaultClassLoader() {
        if (superclass != null) {
            return superclass.getClassLoader();
        } else if (interfaces != null) {
            return interfaces[0].getClassLoader();
        } else {
            return null;
        }
    }

    public void generateClass(ClassVisitor v) throws Exception {
        new EnhancerEmitter(v, getClassName(), superclass, interfaces, filter, callbackTypes, useFactory);
    }

    protected Object firstInstance(Class type) throws Exception {
        if (classOnly) {
            return type;
        } else {
            return createUsingReflection(type);
        }
    }

    protected Object nextInstance(Object instance) {
        if (classOnly) {
            return instance;
        } else if (instance instanceof Factory) {
            if (argumentTypes != null) {
                return ((Factory)instance).newInstance(argumentTypes, arguments, callbacks);
            } else {
                return ((Factory)instance).newInstance(callbacks);
            }
        } else {
            return createUsingReflection(instance.getClass());
        }
    }

    private Object createUsingReflection(Class type) {
        EnhancerEmitter.setThreadCallbacks(type, callbacks);
        if (argumentTypes != null) {
            return ReflectUtils.newInstance(type, argumentTypes, arguments);
        } else {
            return ReflectUtils.newInstance(type);
        }            
    }

    /**
     * Helper method to create an intercepted object.
     * For finer control over the generated instance, use a new instance of Enhancer
     * instead of this static method.
     * @param type class to extend or interface to implement
     * @param callback the callback to use for all methods
     */
    public static Object create(Class type, Callback callback) {
        Enhancer e = new Enhancer();
        e.setSuperclass(type);
        e.setCallback(callback);
        return e.create();
    }

    /**
     * Helper method to create an intercepted object.
     * For finer control over the generated instance, use a new instance of Enhancer
     * instead of this static method.
     * @param type class to extend or interface to implement
     * @param interfaces array of interfaces to implement, or null
     * @param callback the callback to use for all methods
     */
    public static Object create(Class superclass, Class interfaces[], Callback callback) {
        Enhancer e = new Enhancer();
        e.setSuperclass(superclass);
        e.setInterfaces(interfaces);
        e.setCallback(callback);
        return e.create();
    }

    /**
     * Helper method to create an intercepted object.
     * For finer control over the generated instance, use a new instance of Enhancer
     * instead of this static method.
     * @param type class to extend or interface to implement
     * @param interfaces array of interfaces to implement, or null
     * @param filter the callback filter to use when generating a new class
     * @param callbacks callback implementations to use for the enhanced object
     */
    public static Object create(Class superclass, Class[] interfaces, CallbackFilter filter, Callback[] callbacks) {
        Enhancer e = new Enhancer();
        e.setSuperclass(superclass);
        e.setInterfaces(interfaces);
        e.setCallbackFilter(filter);
        e.setCallbacks(callbacks);
        return e.create();
    }
}
