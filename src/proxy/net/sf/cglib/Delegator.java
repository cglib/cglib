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
import java.util.*;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: Delegator.java,v 1.3 2002/12/03 06:49:01 herbyderby Exp $
 */
public class Delegator {
    /* package */ static final Class TYPE = Delegator.class;

    private static final String CLASS_NAME = "net.sf.cglib.Delegator$$CreatedByCGLIB$$";
    private static int index = 0;
    private static final ClassLoader defaultLoader = TYPE.getClassLoader();
    private static final Map loaders = new WeakHashMap();
    private static final Map infoCache = new HashMap();

    private static final DelegatorKey keyFactory =
      (DelegatorKey)KeyFactory.makeFactory(DelegatorKey.class, null);

    /* package */ interface DelegatorKey {
        public Object newInstance(Class[] classes);
    }

    private static String getNextName() {
        return CLASS_NAME + index++;
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
    public static Object makeDelegator(Class[] interfaces, Object[] delegates, ClassLoader loader) {
        return makeDelegatorHelper(keyFactory.newInstance(interfaces), interfaces, delegates, loader);
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
    public static Object makeDelegator(Object[] delegates, ClassLoader loader) {
        Info info = getInfo(delegates);
        Object[] remapped = new Object[info.interfaces.length];
        for (int i = 0; i < remapped.length; i++) {
            remapped[i] = delegates[info.indexes[i]];
        }
        return makeDelegatorHelper(info.key, info.interfaces, remapped, loader);
    }

    /**
     * Returns a Map that describes how interfaces would be delegated.
     * The keys are interfaces (Class objects) that would be implemented
     * by the object returned by <code>makeDelegator(delegates)</code>. For each
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

    synchronized private static Info getInfo(Object[] delegates) {
        // TODO: optimize object->class conversion?
        Class[] classes = new Class[delegates.length];
        for (int i = 0; i < delegates.length; i++) {
            classes[i] = delegates[i].getClass();
        }
        Object key = keyFactory.newInstance(classes);
        Info info = (Info)infoCache.get(key);
        if (info == null)
            infoCache.put(key, info = new Info(delegates));
        return info;
    }

    synchronized private static Object makeDelegatorHelper(Object key,
                                                           Class[] interfaces,
                                                           Object[] delegates,
                                                           ClassLoader loader) {
        if (loader == null) {
            loader = defaultLoader;
        }
        Map factories = (Map)loaders.get(loader);
        if (factories == null) {
            loaders.put(loader, factories = new HashMap());
        }
        Object factory = (Factory)factories.get(key);
        if (factory == null) {
            Class clazz = new DelegatorGenerator(getNextName(), interfaces, loader).define();
            try {
                factory = clazz.getConstructor(Constants.TYPES_OBJECT_ARRAY).newInstance(new Object[]{ delegates });
                factories.put(key, factory);
                return factory;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new CodeGenerationException(e);
            }
        } else {
            return ((Factory)factory).cglib_newInstance(delegates);
        }
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
                    // get first superinterface that has declared methods
                    // this weeds out interfaces with no methods and helps caching
                    while (iface != null && iface.getDeclaredMethods().length == 0) {
                        iface = iface.getSuperclass();
                    }
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
            key = keyFactory.newInstance(interfaces);
        }
    }

    private static Class[] getAllInterfaces(Class clazz) {
        List interfaces = new ArrayList();
        while (!clazz.equals(Constants.TYPE_OBJECT)) {
            interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
            clazz = clazz.getSuperclass();
        }
        return (Class[])interfaces.toArray(new Class[interfaces.size()]);
    }
}    
