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
import junit.framework.*;
import java.io.*;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: TestParallelSorter.java,v 1.5 2003/02/02 03:53:52 herbyderby Exp $
 */
public class TestParallelSorter extends CodeGenTestCase {
    public void testSorts() throws Throwable {
        Object[] data1 = getTestData();
        Object[] data2 = copy(data1);
        Object[] data3 = copy(data1);
        int[] idx1 = getIndexes(data1.length);
        int[] idx2 = getIndexes(data1.length);
        int[] idx3 = getIndexes(data1.length);
        ParallelSorter p1 = ParallelSorter.create(new Object[]{ data1, idx1 });
        ParallelSorter p2 = ParallelSorter.create(new Object[]{ data2, idx2 });
        p1.quickSort(0);
        p2.mergeSort(0);
        compare(data1, data2);
        compare(idx1, idx2);
        p1.quickSort(1);
        compare(idx1, idx3);
        compare(data1, data3);
    }

    private void compare(Object[] data1, Object[] data2) {
        assertTrue(data1.length == data2.length);
        for (int i = 0; i < data1.length; i++) {
            assertTrue(data1[i].equals(data2[i]));
        }
    }

    private void compare(int[] data1, int[] data2) {
        assertTrue(data1.length == data2.length);
        for (int i = 0; i < data1.length; i++) {
            assertTrue(data1[i] == data2[i]);
        }
    }
    
    private int[] getIndexes(int len) {
        int[] idx = new int[len];
        for (int i = 0; i < len; i++) {
            idx[i] = i;
        }
        return idx;
    }

    private Object[] getTestData() throws IOException {
        InputStream in = getClass().getResource("words.txt").openStream();
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        List list = new ArrayList();
        String line;
        int c = 0;
        while ((line = r.readLine()) != null) {
            list.add(line);
            if (c++ == 20) break;
        }
        return list.toArray();
    }

    private Object[] copy(Object[] data) {
        Object[] copy = new Object[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

    public TestParallelSorter(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestParallelSorter.class);
    }
}
