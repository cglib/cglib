/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
import net.sf.cglib.util.*;

/**
 * For the efficient sorting of multiple arrays in parallel.
 * <p>
 * Given two arrays of equal length and varying types, the standard
 * technique for sorting them in parallel is to create a new temporary
 * object for each row, store the objects in a temporary array, sort the
 * array using a custom comparator, and the extract the original values
 * back into their respective arrays. This is wasteful in both time and
 * memory.
 * <p>
 * This class generates bytecode customized to the particular set of
 * arrays you need to sort, in such a way that both arrays are sorted
 * in-place, simultaneously.
 * <p>
 * Two sorting algorithms are provided.
 * Quicksort is best when you only need to sort by a single column, as
 * it requires very few comparisons and swaps. Mergesort is best used
 * when sorting multiple columns, as it is a "stable" sort--that is, it
 * does not affect the relative order of equal objects from previous sorts.
 * <p>
 * The mergesort algorithm here is an "in-place" variant, which while
 * slower, does not require a temporary array.
 *
 * @author Chris Nokleberg
 */
abstract public class ParallelSorter extends SorterTemplate {
    static final Class TYPE = ParallelSorter.class;
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
    private Comparer comparer;
    
    interface ParallelSorterKey {
        public Object newInstance(Class[] classes);
    }

    protected ParallelSorter() {
    }

    abstract protected ParallelSorter cglib_newInstance(Object[] arrays);

    /**
     * Helper method, has same effect as <pre>return create(arrays, null);</pre>
     * @see #create(Object[], ClassLoader)
     */
    public static ParallelSorter create(Object[] arrays) {
        return create(arrays, null);
    }

    /**
     * Create a new ParallelSorter object for a set of arrays. You may
     * sort the arrays multiple times via the same ParallelSorter object.
     * @param arrays An array of arrays to sort. The arrays may be a mix
     * of primitive and non-primitive types, but should all be the same
     * length.
     * @param loader ClassLoader for generated class, uses "current" if null
     */
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

    private int len() {
        return ((Object[])a[0]).length;
    }

    /**
     * Sort the arrays using the quicksort algorithm.
     * @param index array (column) to sort by
     */
    public void quickSort(int index) {
        quickSort(index, 0, len(), null);
    }

    /**
     * Sort the arrays using the quicksort algorithm.
     * @param index array (column) to sort by
     * @param lo starting array index (row), inclusive
     * @param hi ending array index (row), exclusive
     */
    public void quickSort(int index, int lo, int hi) {
        quickSort(index, lo, hi, null);
    }

    /**
     * Sort the arrays using the quicksort algorithm.
     * @param index array (column) to sort by
     * @param cmp Comparator to use if the specified column is non-primitive
     */
    public void quickSort(int index, Comparator cmp) {
        quickSort(index, 0, len(), cmp);
    }

    /**
     * Sort the arrays using the quicksort algorithm.
     * @param index array (column) to sort by
     * @param lo starting array index (row), inclusive
     * @param hi ending array index (row), exclusive
     * @param cmp Comparator to use if the specified column is non-primitive
     */
    public void quickSort(int index, int lo, int hi, Comparator cmp) {
        chooseComparer(index, cmp);
        super.quickSort(lo, hi - 1);
    }

    /**
     * @param index array (column) to sort by
     */
    public void mergeSort(int index) {
        mergeSort(index, 0, len(), null);
    }

    /**
     * Sort the arrays using an in-place merge sort.
     * @param index array (column) to sort by
     * @param lo starting array index (row), inclusive
     * @param hi ending array index (row), exclusive
     */
    public void mergeSort(int index, int lo, int hi) {
        mergeSort(index, lo, hi, null);
    }

    /**
     * Sort the arrays using an in-place merge sort.
     * @param index array (column) to sort by
     * @param lo starting array index (row), inclusive
     * @param hi ending array index (row), exclusive
     */
    public void mergeSort(int index, Comparator cmp) {
        mergeSort(index, 0, len(), cmp);
    }

    /**
     * Sort the arrays using an in-place merge sort.
     * @param index array (column) to sort by
     * @param lo starting array index (row), inclusive
     * @param hi ending array index (row), exclusive
     * @param cmp Comparator to use if the specified column is non-primitive
     */
    public void mergeSort(int index, int lo, int hi, Comparator cmp) {
        chooseComparer(index, cmp);
        super.mergeSort(lo, hi - 1);
    }
    
    private static void validate(Class[] classes) {
        if (classes.length == 0) {
            throw new IllegalArgumentException("No arrays specified to sort");
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
            factory_method(NEW_INSTANCE);
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
                Local T = make_local(type);

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

    private void chooseComparer(int index, Comparator cmp) {
        Object array = a[index];
        Class type = array.getClass().getComponentType();
        if (type.equals(Integer.TYPE)) {
            comparer = new IntComparer((int[])array);
        } else if (type.equals(Long.TYPE)) {
            comparer = new LongComparer((long[])array);
        } else if (type.equals(Double.TYPE)) {
            comparer = new DoubleComparer((double[])array);
        } else if (type.equals(Float.TYPE)) {
            comparer = new FloatComparer((float[])array);
        } else if (type.equals(Short.TYPE)) {
            comparer = new ShortComparer((short[])array);
        } else if (type.equals(Byte.TYPE)) {
            comparer = new ByteComparer((byte[])array);
        } else if (cmp != null) {
            comparer = new ComparatorComparer((Object[])array, cmp);
        } else {
            comparer = new ObjectComparer((Object[])array);
        } 
    }

    protected int compare(int i, int j) {
        return comparer.compare(i, j);
    }

    interface Comparer {
        int compare(int i, int j);
    }

    static class ComparatorComparer implements Comparer {
        private Object[] a;
        private Comparator cmp;

        public ComparatorComparer(Object[] a, Comparator cmp) {
            this.a = a;
            this.cmp = cmp;
        }

        public int compare(int i, int j) {
            return cmp.compare(a[i], a[j]);
        }
    }
    
    static class ObjectComparer implements Comparer {
        private Object[] a;
        public ObjectComparer(Object[] a) { this.a = a; }
        public int compare(int i, int j) {
            return ((Comparable)a[i]).compareTo(a[j]);
        }
    }

    static class IntComparer implements Comparer {
        private int[] a;
        public IntComparer(int[] a) { this.a = a; }
        public int compare(int i, int j) { return a[i] - a[j]; }
    }

    static class LongComparer implements Comparer {
        private long[] a;
        public LongComparer(long[] a) { this.a = a; }
        public int compare(int i, int j) {
            long vi = a[i];
            long vj = a[j];
            return (vi == vj) ? 0 : (vi > vj) ? 1 : -1;
        }
    }

    static class FloatComparer implements Comparer {
        private float[] a;
        public FloatComparer(float[] a) { this.a = a; }
        public int compare(int i, int j) {
            float vi = a[i];
            float vj = a[j];
            return (vi == vj) ? 0 : (vi > vj) ? 1 : -1;
        }
    }
    
    static class DoubleComparer implements Comparer {
        private double[] a;
        public DoubleComparer(double[] a) { this.a = a; }
        public int compare(int i, int j) {
            double vi = a[i];
            double vj = a[j];
            return (vi == vj) ? 0 : (vi > vj) ? 1 : -1;
        }
    }

    static class ShortComparer implements Comparer {
        private short[] a;
        public ShortComparer(short[] a) { this.a = a; }
        public int compare(int i, int j) { return a[i] - a[j]; }
    }

    static class ByteComparer implements Comparer {
        private byte[] a;
        public ByteComparer(byte[] a) { this.a = a; }
        public int compare(int i, int j) { return a[i] - a[j]; }
    }
}
