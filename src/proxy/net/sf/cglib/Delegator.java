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
import java.util.*;

/**
 * <code>Delegator</code> provides a number of static methods that allow
 * multiple objects to be combined into a single larger object. The
 * methods in the generated object simply call the original methods in the
 * underlying "delegate" objects.
 * @version $Id: Delegator.java,v 1.19 2003/06/07 05:51:33 herbyderby Exp $
 */
public class Delegator {
    static final Class TYPE = Delegator.class;

    private static final FactoryCache cache = new FactoryCache();
    private static final ClassLoader defaultLoader = TYPE.getClassLoader();
    private static final Map infoCache = new HashMap();
    private static final ClassNameFactory nameFactory = new ClassNameFactory("CreatedByCGLIB");
    private static final DelegatorKey keyFactory =
      (DelegatorKey)KeyFactory.create(DelegatorKey.class, null);

    interface DelegatorKey {
        public Object newInstance(Class[] classes, boolean multicast);
    }

    private Delegator() { }
    
    // Inner and private because if the makeDelegator(Object[]) constructor
    // was used, the required order of the delegates may be different from
    // what was originally specified, which would be confusion. There is
    // not much overhead associated with going through the cache, anyway.
    interface Factory {
        final Class TYPE = Delegator.Factory.class;
        public Object cglib_newInstance(Object[] delegates);
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
        return makeDelegatorHelper(Object.class, false,
                                   keyFactory.newInstance(interfaces, false),
                                   interfaces, delegates, loader, false);
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
        Object[] remapped = new Object[info.interfaces.length];
        for (int i = 0; i < remapped.length; i++) {
            remapped[i] = delegates[info.indexes[i]];
        }
        return makeDelegatorHelper(Object.class, false, info.key, info.interfaces, remapped, loader, false);
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
            map.put(info.interfaces[i], delegates[info.indexes[i]]);
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
        return createBean(Object.class, beans, false, loader);
    }

    /**
     * Combines an array of JavaBeans into a single "super" bean.
     * Calls to the super bean will delegate to the underlying beans.
     * @param cls the Class to extend, Object is used if null
     * @param beans the list of beans to delegate to
     * @param multicast if false, the first bean is used in the case of property name conflicts;
     * if true, "set" methods will set all applicable beans, and "get" will return the value
     * from the last bean in the list.
     * @param loader The ClassLoader to use. If null uses the one that loaded this class.
     */
    static Object createBean(Class cls, Object[] beans, boolean multicast, ClassLoader loader)
    {
        Class[] classes = ReflectUtils.getClasses(beans);
        Object key = keyFactory.newInstance(classes, multicast);
        return makeDelegatorHelper(cls, multicast, key, classes, beans, loader, true);
    }

    synchronized private static Info getInfo(Object[] delegates) {
        Object key = keyFactory.newInstance(ReflectUtils.getClasses(delegates), false);
        Info info = (Info)infoCache.get(key);
        if (info == null)
            infoCache.put(key, info = new Info(delegates));
        return info;
    }

    private static Object makeDelegatorHelper(Class cls,
                                              boolean multicast,
                                              Object key,
                                              Class[] classes,
                                              Object[] delegates,
                                              ClassLoader loader,
                                              boolean bean) {
        if (cls == null) {
            cls = Object.class;
        }
        if (loader == null) {
            loader = defaultLoader;
        }
        Factory factory;
        synchronized (cache) {
            factory = (Factory)cache.get(loader, key);
            if (factory == null) {
                String className = nameFactory.getNextName(TYPE);
                Class result = new DelegatorGenerator(cls, multicast, className, classes, loader, bean).define();
                factory = (Factory)ReflectUtils.newInstance(result, Constants.TYPES_OBJECT_ARRAY, new Object[1]);
                cache.put(loader, key, factory);
            }
        }
        return factory.cglib_newInstance(delegates);
    }

    private static class Info {
        private Class[] interfaces;
        private int[] indexes;
        private Object key;

        public Info(Object[] incoming) {
            Set seenInterfaces = new HashSet();
            List interfaceList = new LinkedList();
            List indexList = new LinkedList();
            for (int i = 0; i < incoming.length; i++) {
                Object delegate = incoming[i];
                Class[] delegateInterfaces = getAllInterfaces(delegate.getClass());
                for (int j = 0; j < delegateInterfaces.length; j++) {
                    Class iface = delegateInterfaces[j];
                    if (iface != null && !seenInterfaces.contains(iface)) {
                        interfaceList.add(iface);
                        indexList.add(new Integer(i));
                        seenInterfaces.add(iface);
                    }
                }
            }
            interfaces = (Class[])interfaceList.toArray(new Class[interfaceList.size()]);
            indexes = new int[interfaces.length];
            Iterator it = indexList.iterator();
            for (int i = 0; it.hasNext(); i++) {
                indexes[i] = ((Integer)it.next()).intValue();
            }
            key = keyFactory.newInstance(interfaces, false);
        }
    }

    private static Class[] getAllInterfaces(Class clazz) {
        List interfaces = new ArrayList();
        while (!clazz.equals(Object.class)) {
            interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
            clazz = clazz.getSuperclass();
        }
        return (Class[])interfaces.toArray(new Class[interfaces.size()]);
    }
}    
