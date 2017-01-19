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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestTinyBitSet extends TestCase {

    private TinyBitSet b = new TinyBitSet();

    public void testGetSetClear() {
        // get, set, clear, set bits 0 - 31
        for (int index = 0; index < 32; index++) {
            assertFalse(b.get(index));
            b.set(index);
            assertTrue(b.get(index));
            b.clear(index);
            assertFalse(b.get(index));
            b.set(index);
        }
        // after setting bits 0-31, get bits 32 and above reports true;
        assertTrue(b.get(32));
        assertTrue(b.get(255));
        assertTrue(b.get(256));
        assertTrue(b.get(1000000));
    }

    public void testGetSetClear2() {
        for (int index = 2; index > 0; index *= 2) {
            b.set(index);
            assertTrue(b.get(index));
            b.clear(index);
            assertFalse(b.get(index));
            b.set(index);
        }
    }

    public void testLength() {
        assertEquals(0, b.length());
        b.set(10);
        assertEquals(11, b.length());
        b.set(15);
        assertEquals(16, b.length());
        b.set(14);
        assertEquals(16, b.length());
    }

    public void testCardinality() {
        assertEquals(0, b.cardinality());
        b.set(1);
        assertEquals(1, b.cardinality());
        b.set(4);
        assertEquals(2, b.cardinality());
        b.set(10);
        assertEquals(3, b.cardinality());
        b.set(10);
        assertEquals(3, b.cardinality());
        b.clear(10);
        assertEquals(2, b.cardinality());
    }

    /** Causes infinite loop */
    public void set31InfiniteLoopCardinality() {
        b.set(31);
        assertEquals(1, b.cardinality()); // infinite loop...
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
