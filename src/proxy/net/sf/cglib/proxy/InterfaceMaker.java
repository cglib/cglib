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

import java.lang.reflect.*;
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

/**
 * Generates new interfaces at runtime.
 * By passing a generated interface to the Enhancer's list of interfaces to
 * implement, you can make your enhanced classes handle an arbitrary set
 * of method signatures.
 * @author Chris Nokleberg
 * @version $Id: InterfaceMaker.java,v 1.1 2004/03/27 17:53:23 herbyderby Exp $
 */
public class InterfaceMaker extends AbstractClassGenerator
{
    private static final Source SOURCE = new Source(InterfaceMaker.class.getName());
    private Map signatures = new HashMap();

    /**
     * Create a new <code>InterfaceMaker</code>. A new <code>InterfaceMaker</code>
     * object should be used for each generated interface, and should not
     * be shared across threads.
     */
    public InterfaceMaker() {
        super(SOURCE);
    }

    /**
     * Add a method signature to the interface.
     * @param sig the method signature to add to the interface
     * @param exceptions an array of exception types to declare for the method
     */
    public void add(Signature sig, Type[] exceptions) {
        signatures.put(sig, exceptions);
    }

    /**
     * Add a method signature to the interface. The method modifiers are ignored,
     * since interface methods are by definition abstract and public.
     * @param method the method to add to the interface
     */
    public void add(Method method) {
        add(ReflectUtils.getSignature(method),
            ReflectUtils.getExceptionTypes(method));
    }

    /**
     * Add all the public methods in the specified class.
     * Methods from superclasses are included, except for methods declared in the base
     * Object class (e.g. <code>getClass</code>, <code>equals</code>, <code>hashCode</code>).
     * @param class the class containing the methods to add to the interface
     */
    public void add(Class clazz) {
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (!m.getDeclaringClass().getName().equals("java.lang.Object")) {
                add(m);
            }
        }
    }

    /**
     * Create an interface using the current set of method signatures.
     */
    public Class create() {
        setUseCache(false);
        return (Class)super.create(this);
    }

    protected ClassLoader getDefaultClassLoader() {
        return null;
    }
    
    protected Object firstInstance(Class type) {
        return type;
    }

    protected Object nextInstance(Object instance) {
        throw new IllegalStateException("InterfaceMaker does not cache");
    }

    public void generateClass(ClassVisitor v) throws Exception {
        ClassEmitter ce = new ClassEmitter(v);
        ce.begin_class(Constants.ACC_PUBLIC | Constants.ACC_INTERFACE,
                       getClassName(),
                       null,
                       null,
                       Constants.SOURCE_FILE);
        for (Iterator it = signatures.keySet().iterator(); it.hasNext();) {
            Signature sig = (Signature)it.next();
            Type[] exceptions = (Type[])signatures.get(sig);
            ce.begin_method(Constants.ACC_PUBLIC | Constants.ACC_ABSTRACT,
                            sig,
                            exceptions,
                            null).end_method();
        }
        ce.end_class();
    }
}
