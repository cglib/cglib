package net.sf.cglib;

import java.util.*;

abstract /* package */ class SorterTemplate {
    private static final int QUICKSORT_THRESHOLD = 7;

    abstract protected void swap(int i, int j);

    public void quickSort(Object[] a, int lo, int hi, Comparator cmp) {
        quickSortHelper(a, lo, hi, cmp);
        insertionSort(a, lo, hi, cmp);
    }

    public void quickSort(Comparable[] a, int lo, int hi) {
        quickSortHelper(a, lo, hi);
        insertionSort(a, lo, hi);
    }
    
    public void mergeSort(Object[] a, int lo, int hi, Comparator cmp) {
        // TODO
    }

    public void mergeSort(Comparable[] a, int lo, int hi) {
        // TODO
    }
    
    private void quickSortHelper(Object[] a, int lo, int hi, Comparator cmp) {
        for (;;) {
            int diff = hi - lo;
            if (diff < QUICKSORT_THRESHOLD)
                break;
            int i = (hi + lo) / 2;
            if (cmp.compare(a[lo], a[i]) > 0)
                swap(lo, i);
            if (cmp.compare(a[lo], a[hi]) > 0)
                swap(lo, hi);
            if (cmp.compare(a[i], a[hi]) > 0)
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

    private void quickSortHelper(Comparable[] a, int lo, int hi) {
        for (;;) {
            int diff = hi - lo;
            if (diff < QUICKSORT_THRESHOLD)
                break;
            int i = (hi + lo) / 2;
            if (a[lo].compareTo(a[i]) > 0)
                swap(lo, i);
            if (a[lo].compareTo(a[hi]) > 0)
                swap(lo, hi);
            if (a[i].compareTo(a[hi]) > 0)
                swap(i, hi);
            int j = hi - 1;
            swap(i, j);
            i = lo;
            Object v = a[j];
            for (;;) {
                while (a[++i].compareTo(v) < 0) /* nothing */;
                while (a[--j].compareTo(v) > 0) /* nothing */;
                if (j < i) break;
                swap(i, j);
            }
            swap(i, hi - 1);
            if (j - lo <= hi - i + 1) {
                quickSortHelper(a, lo, j);
                lo = i + 1;
            } else {
                quickSortHelper(a, i + 1, hi);
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

    private void insertionSort(Comparable[] a, int lo, int hi) {
        for (int i = lo + 1 ; i <= hi; i++) {
            for (int j = i; j > lo; j--) {
                if (a[j - 1].compareTo(a[j]) > 0) {
                    swap(j - 1, j);
                } else {
                    break;
                }
            }
        }
    }
}
