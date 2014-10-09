/*
 * Copyright 2003 The Apache Software Foundation
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
package net.sf.cglib.core;

import net.sf.cglib.CodeGenTestCase;
import junit.framework.*;

public class TestTinyBitSet extends TestCase {
    public void testGetSetClear() {
        TinyBitSet b = new TinyBitSet();
        assertTrue(!b.get(5));
        b.set(5);
        assertTrue(b.get(5));
        b.clear(5);
        assertTrue(!b.get(5));
    }
    
    public void testLength() {
        TinyBitSet b = new TinyBitSet();
        b.set(10);
        assertTrue(b.length() == 11);
        b.set(15);
        assertTrue(b.length() == 16);
        b.set(14);
        assertTrue(b.length() == 16);
    }

    public void testCardinality() {
        TinyBitSet b = new TinyBitSet();
        assertTrue(b.cardinality() == 0);
        b.set(1);
        assertTrue(b.cardinality() == 1);
        b.set(4);
        assertTrue(b.cardinality() == 2);
        b.set(10);
        assertTrue(b.cardinality() == 3);
        b.set(10);
        assertTrue(b.cardinality() == 3);
        b.clear(10);
        assertTrue(b.cardinality() == 2);
    }

    public TestTinyBitSet(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestTinyBitSet.class);
    }
}
