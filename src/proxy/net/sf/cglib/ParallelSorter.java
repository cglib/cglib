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
import java.util.Comparator;

abstract public class ParallelSorter extends SorterTemplate {
    /* package */ static final Class TYPE = ParallelSorter.class;
    private static final FactoryCache cache = new FactoryCache();
    private static final ClassLoader defaultLoader = TYPE.getClassLoader();
    private static final ClassNameFactory nameFactory = new ClassNameFactory("SortedByCGLIB");
    private static final ParallelSorterKey keyFactory =
      (ParallelSorterKey)KeyFactory.create(ParallelSorterKey.class, null);
    private static final Method NEW_INSTANCE =
      ReflectUtils.findMethod("ParallelSorter.cglib_newInstance(Object[])");
    private static final Method SWAP_METHOD =
      ReflectUtils.findMethod("SorterTemplate.swap(int, int)");

    protected Object[] a;

    // should be package-protected but causes problems on jdk1.2
    public interface ParallelSorterKey {
        public Object newInstance(Class[] classes);
    }

    protected ParallelSorter() {
    }

    abstract protected ParallelSorter cglib_newInstance(Object[] arrays);

    public static ParallelSorter create(Object[] arrays) {
        return create(arrays, null);
    }

    public static ParallelSorter create(Object[] arrays, ClassLoader loader) {
        if (loader == null) {
            loader = defaultLoader;
        }
        Class[] classes = ReflectUtils.getClasses(arrays);
        Object key = keyFactory.newInstance(classes);
        ParallelSorter factory;
        synchronized (cache) {
            factory = (ParallelSorter)cache.get(loader, key);
            if (factory == null) {
                validate(classes);
                String className = nameFactory.getNextName(TYPE);
                Class result = new Generator(className, classes, loader).define();
                factory = (ParallelSorter)ReflectUtils.newInstance(result,
                                                                   Constants.TYPES_OBJECT_ARRAY,
                                                                   new Object[]{ arrays });
                cache.put(loader, key, factory);
                return factory;
            }
        }
        return factory.cglib_newInstance(arrays);
    }

    public void quickSort(int index) {
        super.quickSort((Comparable[])a[index], 0, ((Comparable[])a[index]).length - 1);
    }

    public void quickSort(int index, int lo, int hi) {
        super.quickSort((Comparable[])a[index], lo, hi);
    }

    public void quickSort(int index, Comparator cmp) {
        super.quickSort((Object[])a[index], 0, ((Object[])a[index]).length - 1, cmp);
    }

    public void quickSort(int index, int lo, int hi, Comparator cmp) {
        super.quickSort((Object[])a[index], lo, hi, cmp);
    }

    public void mergeSort(int index) {
        super.mergeSort((Comparable[])a[index], 0, ((Comparable[])a[index]).length - 1);
    }

    public void mergeSort(int index, int lo, int hi) {
        super.mergeSort((Comparable[])a[index], lo, hi);
    }

    public void mergeSort(int index, Comparator cmp) {
        super.mergeSort((Object[])a[index], 0, ((Object[])a[index]).length - 1, cmp);
    }

    public void mergeSort(int index, int lo, int hi, Comparator cmp) {
        super.mergeSort((Object[])a[index], lo, hi, cmp);
    }
    
    private static void validate(Class[] classes) {
        if (classes.length == 0 ||
            !classes[0].isArray() ||
            classes[0].getComponentType().isPrimitive()) {
            throw new IllegalArgumentException("First argument must be a non-primitive array");
        }
        for (int i = 0; i < classes.length; i++) {
            if (!classes[i].isArray()) {
                throw new IllegalArgumentException(classes[i] + " is not an array");
            }
        }
    }

    private static class Generator extends CodeGenerator {
        private Class[] classes;

        private String getFieldName(int index) {
            return "FIELD_" + index;
        }

        public Generator(String className, Class[] classes, ClassLoader loader) {
            super(className, ParallelSorter.class, loader);
            this.classes = classes;
        }

        protected void generate() throws NoSuchFieldException {
            generateFactoryMethod(NEW_INSTANCE);
            generateConstructor();
            generateSwap();
        }

        private void generateConstructor() throws NoSuchFieldException {
            begin_constructor(Constants.TYPES_OBJECT_ARRAY);
            load_this();
            super_invoke_constructor();
            load_this();
            load_arg(0);
            super_putfield("a");
            for (int i = 0; i < classes.length; i++) {
                declare_field(Modifier.PRIVATE, classes[i], getFieldName(i));
                load_this();
                load_arg(0);
                push(i);
                aaload();
                checkcast(classes[i]);
                putfield(getFieldName(i));
            }
            return_value();
            end_method();
        }

        private void generateSwap() {
            begin_method(SWAP_METHOD);
            for (int i = 0; i < classes.length; i++) {
                Class type = classes[i];
                Class component = type.getComponentType();
                Object T = make_local(type);

                load_this();
                getfield(getFieldName(i));
                store_local(T);

                load_local(T);
                load_arg(0);

                load_local(T);
                load_arg(1);
                array_load(component);
                
                load_local(T);
                load_arg(1);

                load_local(T);
                load_arg(0);
                array_load(component);

                array_store(component);
                array_store(component);
            }
            return_value();
            end_method();
        }
    }
}
