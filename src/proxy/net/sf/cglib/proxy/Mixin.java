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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;

/**
 * <code>Mixin</code> allows
 * multiple objects to be combined into a single larger object. The
 * methods in the generated object simply call the original methods in the
 * underlying "delegate" objects.
 * @author Chris Nokleberg
 * @version $Id: Mixin.java,v 1.3 2004/01/25 22:21:00 herbyderby Exp $
 */
abstract public class Mixin {
    private static final MixinKey KEY_FACTORY =
      (MixinKey)KeyFactory.create(MixinKey.class, KeyFactory.CLASS_BY_NAME);
    private static final Map ROUTE_CACHE = Collections.synchronizedMap(new HashMap());

    public static final int STYLE_INTERFACES = 0;
    public static final int STYLE_BEANS = 1;
    public static final int STYLE_EVERYTHING = 2;

    interface MixinKey {
        public Object newInstance(int style, Class[] classes, int[] route);
    }

    abstract public Mixin newInstance(Object[] delegates);

    /**
     * Helper method to create an interface mixin. For finer control over the
     * generated instance, use a new instance of <code>Mixin</code>
     * instead of this static method.
     * TODO
     */
    public static Mixin create(Object[] delegates) {
        Generator gen = new Generator();
        gen.setDelegates(delegates);
        return gen.create();
    }

    /**
     * Helper method to create an interface mixin. For finer control over the
     * generated instance, use a new instance of <code>Mixin</code>
     * instead of this static method.
     * TODO
     */
    public static Mixin create(Class[] interfaces, Object[] delegates) {
        Generator gen = new Generator();
        gen.setClasses(interfaces);
        gen.setDelegates(delegates);
        return gen.create();
    }

    /**
     * Helper method to create a bean mixin. For finer control over the
     * generated instance, use a new instance of <code>Mixin</code>
     * instead of this static method.
     * TODO
     */
    public static Mixin createBean(Object[] beans) {
        Generator gen = new Generator();
        gen.setStyle(STYLE_BEANS);
        gen.setDelegates(beans);
        return gen.create();
    }
    
    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(Mixin.class.getName());

        private Class[] classes;
        private Object[] delegates;
        private int style = STYLE_INTERFACES;
        
        private int[] route;

        public Generator() {
            super(SOURCE);
        }

        protected ClassLoader getDefaultClassLoader() {
            return delegates[0].getClass().getClassLoader(); // is this right?
        }

        public void setStyle(int style) {
            switch (style) {
            case STYLE_INTERFACES:
            case STYLE_BEANS:
            case STYLE_EVERYTHING:
                this.style = style;
                break;
            default:
                throw new IllegalArgumentException("Unknown mixin style: " + style);
            }
        }

        public void setClasses(Class[] classes) {
            this.classes = classes;
        }

        public void setDelegates(Object[] delegates) {
            this.delegates = delegates;
        }

        public Mixin create() {
            switch (style) {
            case STYLE_INTERFACES:
                if (classes == null) {
                    if (delegates == null) {
                        throw new IllegalStateException("Either classes or delegates must be set");
                    }
                    Route r = route(delegates);
                    classes = r.classes;
                    route = r.route;
                }
                break;
            case STYLE_BEANS:
                // fall-through
            case STYLE_EVERYTHING:
                if (delegates == null) {
                    throw new IllegalStateException("Delegates must be set");
                }
                Class[] temp = ReflectUtils.getClasses(delegates);
                if (classes != null) {
                    if (classes.length != temp.length) {
                        throw new IllegalStateException("Specified classes are incompatible with delegates");
                    }
                    for (int i = 0; i < classes.length; i++) {
                        if (!classes[i].isAssignableFrom(temp[i])) {
                            throw new IllegalStateException("Specified class " + classes[i] + " is incompatible with delegate class " + temp[i] + " (index " + i + ")");
                        }
                    }
                } else {
                    classes = temp;
                }
            }
            setNamePrefix(classes[ReflectUtils.findPackageProtected(classes)].getName());
            return (Mixin)super.create(KEY_FACTORY.newInstance(style, classes, route));
        }

        public void generateClass(ClassVisitor v) {
            switch (style) {
            case STYLE_INTERFACES:
                new MixinEmitter(v, getClassName(), classes, route);
                break;
            case STYLE_BEANS:
                new MixinBeanEmitter(v, getClassName(), classes);
                break;
            case STYLE_EVERYTHING:
                new MixinEverythingEmitter(v, getClassName(), classes);
                break;
            }
        }

        protected Object firstInstance(Class type) {
            return ((Mixin)ReflectUtils.newInstance(type)).newInstance(delegates);
        }

        protected Object nextInstance(Object instance) {
            return ((Mixin)instance).newInstance(delegates);
        }
    }

    public static Class[] getClasses(Object[] delegates) {
        return (Class[])route(delegates).classes.clone();
    }

//     public static int[] getRoute(Object[] delegates) {
//         return (int[])route(delegates).route.clone();
//     }
        
    private static Route route(Object[] delegates) {
        Object key = ClassesKey.create(delegates);
        Route route = (Route)ROUTE_CACHE.get(key);
        if (route == null) {
            ROUTE_CACHE.put(key, route = new Route(delegates));
        }
        return route;
    }

    private static class Route
    {
        private Class[] classes;
        private int[] route;

        Route(Object[] delegates) {
            Map map = new HashMap();
            ArrayList collect = new ArrayList();
            for (int i = 0; i < delegates.length; i++) {
                Class delegate = delegates[i].getClass();
                collect.clear();
                ReflectUtils.addAllInterfaces(delegate, collect);
                for (Iterator it = collect.iterator(); it.hasNext();) {
                    Class iface = (Class)it.next();
                    if (!map.containsKey(iface)) {
                        map.put(iface, new Integer(i));
                    }
                }
            }
            classes = new Class[map.size()];
            route = new int[map.size()];
            int index = 0;
            for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                Class key = (Class)it.next();
                classes[index] = key;
                route[index] = ((Integer)map.get(key)).intValue();
                index++;
            }
        }
    }
}
