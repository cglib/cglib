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
package net.sf.cglib.delegates;

import java.lang.reflect.*;
import net.sf.cglib.*;
import net.sf.cglib.util.*;

/**
 * @author Chris Nokleberg
 * @version $Id: ConstructorDelegate.java,v 1.1 2003/09/09 16:15:10 herbyderby Exp $
 */
public abstract class ConstructorDelegate {
    private static final FactoryCache CACHE = new FactoryCache(ConstructorDelegate.class);
    private static final ConstructorKey KEY_FACTORY =
      (ConstructorKey)KeyFactory.create(ConstructorKey.class, null);
    
    interface ConstructorKey {
        public Object newInstance(Class declaring, Class iface);
    }

    protected ConstructorDelegate() {
    }
   
    public static Object create(Class declaring, Class iface) {
        return create(declaring, iface, declaring.getClassLoader());
    }
    
    public static Object create(final Class declaring, final Class iface, ClassLoader loader) {
        Object key = KEY_FACTORY.newInstance(declaring, iface);
        return CACHE.get(loader, key, new FactoryCache.AbstractCallback() {
            public BasicCodeGenerator newGenerator() {
                return new Generator(declaring, iface);
            }
        });
    }

    private static class Generator extends CodeGenerator {
        private Constructor constructor;
        private Method newInstance;

        public Generator(Class declaring, Class iface) {
            try {
                newInstance = ReflectUtils.findNewInstance(iface);
                if (!newInstance.getReturnType().isAssignableFrom(declaring)) {
                    throw new IllegalArgumentException("incompatible return type");
                }
                constructor = declaring.getDeclaredConstructor(newInstance.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("interface does not match any known constructor");
            }
            setSuperclass(ConstructorDelegate.class);
            setNamePrefix(declaring.getName());
            addInterface(newInstance.getDeclaringClass());
        }

        protected void generate() {
            null_constructor();

            begin_method(newInstance);
            new_instance(constructor.getDeclaringClass());
            dup();
            load_args();
            invoke(constructor);
            return_value();
            end_method();
        }
    }
}
