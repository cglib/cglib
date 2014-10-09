/*
 * Copyright 2003,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.cglib.CodeGenTestCase;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: TestParallelSorter.java,v 1.4 2004/06/24 21:15:13 herbyderby Exp $
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
        InputStream in = getClass().getResourceAsStream("words.txt");
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
    
    public void perform(ClassLoader loader) throws Throwable {
    }
    
    public void testFailOnMemoryLeak() throws Throwable {
    }
    
}
