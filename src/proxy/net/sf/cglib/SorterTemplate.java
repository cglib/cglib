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

import java.util.*;

abstract class SorterTemplate {
    private static final int MERGESORT_THRESHOLD = 12;
    private static final int QUICKSORT_THRESHOLD = 7;
    private static final Comparator DEFAULT_COMPARATOR = new DefaultComparator();

    private static class DefaultComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return ((Comparable)o1).compareTo(o2);
        }
    }

    abstract protected void swap(int i, int j);

    public void quickSort(Object[] a, int lo, int hi) {
        quickSort(a, lo, hi, DEFAULT_COMPARATOR);
    }

    public void quickSort(Object[] a, int lo, int hi, Comparator cmp) {
        quickSortHelper(a, lo, hi, cmp);
        insertionSort(a, lo, hi, cmp);
    }


    private void quickSortHelper(Object[] a, int lo, int hi, Comparator cmp) {
        for (;;) {
            int diff = hi - lo;
            if (diff <= QUICKSORT_THRESHOLD)
                break;
            int i = (hi + lo) / 2;
            if (cmp.compare(a[lo], a[i]) > 0)
                swap(lo, i);
            if (cmp.compare(a[lo], a[hi - 1]) > 0)
                swap(lo, hi);
            if (cmp.compare(a[i], a[hi - 1]) > 0)
                swap(i, hi);
            int j = hi - 1;
            swap(i, j);
            i = lo;
            Object v = a[j];
            for (;;) {
                while (cmp.compare(a[++i], v) < 0) /* nothing */;
                while (cmp.compare(a[--j], v) > 0) /* nothing */;
                if (j < i) break;
                swap(i, j);
            }
            swap(i, hi - 1);
            if (j - lo <= hi - i + 1) {
                quickSortHelper(a, lo, j, cmp);
                lo = i + 1;
            } else {
                quickSortHelper(a, i + 1, hi, cmp);
                hi = j;
            }
        }
    }
    
    private void insertionSort(Object[] a, int lo, int hi, Comparator cmp) {
        for (int i = lo + 1 ; i <= hi; i++) {
            for (int j = i; j > lo; j--) {
                if (cmp.compare(a[j - 1], a[j]) > 0) {
                    swap(j - 1, j);
                } else {
                    break;
                }
            }
        }
    }

    public void mergeSort(Object[] a, int lo, int hi) {
        mergeSort(a, lo, hi, DEFAULT_COMPARATOR);
    }

    public void mergeSort(Object[] a, int lo, int hi, Comparator cmp) {
        int diff = hi - lo;
        if (diff <= MERGESORT_THRESHOLD) {
            insertionSort(a, lo, hi, cmp);
            return;
        }
        int mid = lo + diff / 2;
        mergeSort(a, lo, mid, cmp);
        mergeSort(a, mid, hi, cmp);
        merge(a, cmp, lo, mid, hi, mid - lo, hi - mid);
    }

    private void merge(Object[] a, Comparator cmp, int lo, int pivot, int hi, int len1, int len2) {
        if (len1 == 0 || len2 == 0) {
            return;
        }
        if (len1 + len2 == 2) {
            if (cmp.compare(a[pivot], a[lo]) < 0) {
                swap(pivot, lo);
            }
            return;
        }
        int first_cut, second_cut;
        int len11, len22;
        if (len1 > len2) {
            len11 = len1 / 2;
            first_cut = lo + len11;
            second_cut = lower(a, cmp, pivot, hi, first_cut);
            len22 = second_cut - pivot;
        } else {
            len22 = len2 / 2;
            second_cut = pivot + len22;
            first_cut = upper(a, cmp, lo, pivot, second_cut);
            len11 = first_cut - lo;
        }
        rotate(a, first_cut, pivot, second_cut);
        int new_mid = first_cut + len22;
        merge(a, cmp, lo, first_cut, new_mid, len11, len22);
        merge(a, cmp, new_mid, second_cut, hi, len1 - len11, len2 - len22);
    }

    private void rotate(Object[] a, int lo, int mid, int hi) {
        int lot, hit;
        lot = lo; hit = mid - 1;
        while (lot < hit) swap(lot++, hit--);
        lot = mid; hit = hi - 1;
        while (lot < hit) swap(lot++, hit--);
        lot = lo; hit = hi - 1;
        while (lot < hit) swap(lot++, hit--);
    }

    private int lower(Object[] a, Comparator cmp, int lo, int hi, int val) {
        int len = hi - lo;
        while (len > 0) {
            int half = len / 2;
            int mid= lo + half;
            if (cmp.compare(a[mid], a[val]) < 0) {
                lo = mid + 1;
                len = len - half -1;
            } else {
                len = half;
            }
        }
        return lo;
    }

    private int upper(Object[] a, Comparator cmp, int lo, int hi, int val) {
        int len = hi - lo;
        while (len > 0) {
            int half = len / 2;
            int mid = lo + half;
            if (cmp.compare(a[val], a[mid]) < 0) {
                len = half;
            } else {
                lo = mid + 1;
                len = len - half -1;
            }
        }
        return lo;
    }
}
