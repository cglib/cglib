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
import java.util.*;
import net.sf.cglib.util.*;

/**
 * <code>Delegator</code> provides a number of static methods that allow
 * multiple objects to be combined into a single larger object. The
 * methods in the generated object simply call the original methods in the
 * underlying "delegate" objects.
 * @version $Id: Delegator.java,v 1.23 2003/09/04 18:53:46 herbyderby Exp $
 * @author Chris Nokleberg
 */
public class Delegator {
    private static final FactoryCache cache = new FactoryCache(Delegator.class);
    private static final Map infoCache = Collections.synchronizedMap(new HashMap());
    private static final DelegatorKey KEY_FACTORY =
      (DelegatorKey)KeyFactory.create(DelegatorKey.class, null);

    interface DelegatorKey {
        public Object newInstance(Class type, Class[] classes, int[] routing);
    }

    private Delegator() { }

    public interface Factory {
        final Class TYPE = Delegator.Factory.class;
        public Object newInstance(Object[] delegates);
    }

    /**
     * Returns an object that implements all of the specified
     * interfaces. For each interface, all methods are delegated to the
     * respective object in the delegates argument array.
     * @param interfaces the array of interfaces to implement
     * @param delegates The array of delegates. Must be the same length
     * as the interface array, and each delegates must implements the
     * corresponding interface.
     * @param loader The ClassLoader to use. If null uses the one that
     * loaded this class.
     * @return the dynamically created object
     */
    public static Object create(Class[] interfaces, Object[] delegates, ClassLoader loader) {
        return createHelper(null, interfaces, delegates, null, loader);
    }

    /**
     * Returns an object that implements all of the specified
     * interfaces. For each interface, all methods are delegated to the
     * respective object in the delegates argument array.
     * @param type the Class to extend, uses Object if null
     * @param interfaces the array of interfaces to implement
     * @param delegates The array of delegates. Must be the same length
     * as the interface array, and each delegates must implements the
     * corresponding interface.
     * @param loader The ClassLoader to use. If null uses the one that
     * loaded this class.
     * @return the dynamically created object
     */
    public static Object create(Class type, Class[] interfaces, Object[] delegates, ClassLoader loader) {
        return createHelper(type, interfaces, delegates, null, loader);
    }

    /**
     * Returns an object that implements all of the specified
     * interfaces. For each interface, all methods are delegated to the
     * respective object in the delegates argument array.
     * @param type the Class to extend, uses Object if null
     * @param interfaces the array of interfaces to implement
     * @param delegates The array of delegates. If the routing parameter
     * is null, this must be the same length as the interface array. Each delegate
     * must implement the interfaces delegated to it.
     * @param routing An optional routing table. Must be null, or the same length
     * as the interfaces array. If non null, the values represent which delegate
     * the corresponding interface should be mapped to. If null, a 1:1 correspondence
     * is assumed.
     * @param loader The ClassLoader to use. If null uses the one that
     * loaded this class.
     * @return the dynamically created object
     */
    public static Object create(Class type,
                                Class[] interfaces,
                                Object[] delegates,
                                int[] routing,
                                ClassLoader loader) {
        return createHelper(type, interfaces, delegates, routing, loader);
    }

    /**
     * Returns an object that implements all of the interfaces
     * implemented by the specified objects. For each interface, all
     * methods are delegated to the first object in the argument array
     * which implements the interface.
     * <p>
     * <b>Note:</b> interfaces which have no methods (marker interfaces)
     * are not implemented by the returned object.
     * @param delegates the array of delegates
     * @param loader The ClassLoader to use. If null uses the one that
     * loaded this class.
     * @return the dynamically created object
     * @see #getInterfaceMap(Object[])
     */
    public static Object create(Object[] delegates, ClassLoader loader) {
        Info info = getInfo(delegates);
        return createHelper(null, info.interfaces, delegates, info.routing, loader);
    }

    /**
     * Returns a Map that describes how interfaces would be delegated.
     * The keys are interfaces (Class objects) that would be implemented
     * by the object returned by the <code>create</code> methods. For each
     * interface, the Map value is the objects from the argument array
     * that would be used as a delegate.
     * @param delegates the array of delegates
     * @return the Map of interfaces -> delegates
     * @see #makeDelegator(Object[])
     */
    public static Map getInterfaceMap(Object[] delegates) {
        Info info = getInfo(delegates);
        Map map = new HashMap();
        for (int i = 0; i < info.interfaces.length; i++) {
            map.put(info.interfaces[i], delegates[info.routing[i]]);
        }
        return map;
    }

    /**
     * Combines an array of JavaBeans into a single "super" bean.
     * Calls to the super bean will delegate to the underlying beans.
     * In the case of a property name conflicts, the first bean is used.
     * @param beans the list of beans to delegate to
     * @param loader The ClassLoader to use. If null uses the one that loaded this class.
     * @return the dynamically created bean
     */
    public static Object createBean(Object[] beans, ClassLoader loader) {
        return createBean(null, beans, loader);
    }

    /**
     * Combines an array of JavaBeans into a single "super" bean.
     * Calls to the super bean will delegate to the underlying beans.
     * @param type the Class to extend
     * @param beans the list of beans to delegate to
     * if true, "set" methods will set all applicable beans, and "get" will return the value
     * from the last bean in the list.
     * @param loader The ClassLoader to use. If null uses the one that loaded this class.
     */
    public static Object createBean(Class type, Object[] beans, ClassLoader loader) {
        return createHelper(type, ReflectUtils.getClasses(beans), beans, null, loader);
    }

    private static Info getInfo(Object[] delegates) {
        Object key = new ClassesKey(delegates);
        Info info = (Info)infoCache.get(key);
        if (info == null) {
            infoCache.put(key, info = new Info(delegates));
        }
        return info;
    }

    private static Object createHelper(final Class type,
                                       final Class[] classes,
                                       final Object[] delegates,
                                       final int[] routing,
                                       ClassLoader loader) {
        Object key = KEY_FACTORY.newInstance(type, classes, routing);
        return cache.get(loader, key, new FactoryCache.AbstractCallback() {
                public BasicCodeGenerator newGenerator() {
                    return new DelegatorGenerator(type, classes, routing);
                }
                public Object newInstance(Object factory, boolean isNew) {
                    return ((Factory)factory).newInstance(delegates);
                }
            });
    }

    private static class Info {
        Class[] interfaces;
        int[] routing;

        public Info(Object[] incoming) {
            Map map = new HashMap();
            for (int i = 0; i < incoming.length; i++) {
                Class delegate = incoming[i].getClass();
                Iterator it = collectAllInterfaces(delegate, new ArrayList()).iterator();
                while (it.hasNext()) {
                    Class iface = (Class)it.next();
                    if (!map.containsKey(iface)) {
                        map.put(iface, new Integer(i));
                    }
                }
            }
            interfaces = new Class[map.size()];
            routing = new int[map.size()];
            int index = 0;
            for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                Class key = (Class)it.next();
                interfaces[index] = key;
                routing[index] = ((Integer)map.get(key)).intValue();
                index++;
            }
        }
    }

    private static List collectAllInterfaces(Class type, List list) {
        if (!type.equals(Object.class)) {
            list.addAll(Arrays.asList(type.getInterfaces()));
            collectAllInterfaces(type.getSuperclass(), list);
        }
        return list;
    }
}
