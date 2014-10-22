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
package net.sf.cglib.reflect;

import java.lang.reflect.Method;
import junit.framework.*;

/**
 * @version $Id: TestDelegates.java,v 1.4 2004/06/24 21:15:16 herbyderby Exp $
 */
public class TestDelegates extends net.sf.cglib.CodeGenTestCase {

    public interface StringMaker {
        Object newInstance(char[] buf, int offset, int count);
    }

    public void testConstructor() throws Throwable {
        StringMaker maker = (StringMaker)ConstructorDelegate.create(String.class, StringMaker.class);
        assertTrue("nil".equals(maker.newInstance("vanilla".toCharArray(), 2, 3)));
    }

    public interface Substring {
        String substring(int start, int end);
    }

    public interface Substring2 {
        Object anyNameAllowed(int start, int end);
    }

    public interface IndexOf {
        int indexOf(String str, int fromIndex);
    }

    public interface Format {
        String format(String format, Object... args);
    }

    public void testFancy() throws Throwable {
        Substring delegate = (Substring)MethodDelegate.create("CGLIB", "substring", Substring.class);
        assertTrue("LI".equals(delegate.substring(2, 4)));
    }

    public void testFancyNames() throws Throwable {
        Substring2 delegate = (Substring2)MethodDelegate.create("CGLIB", "substring", Substring2.class);
        assertTrue("LI".equals(delegate.anyNameAllowed(2, 4)));
    }

    public void testFancyTypes() throws Throwable {
        String test = "abcabcabc";
        IndexOf delegate = (IndexOf)MethodDelegate.create(test, "indexOf", IndexOf.class);
        assertTrue(delegate.indexOf("ab", 1) == test.indexOf("ab", 1));
    }

    public void testVarArgs() throws Throwable {
        String formatStr = "Time: %d";
        long time = System.currentTimeMillis();
        Format delegate = (Format) MethodDelegate.createStatic(String.class, "format", Format.class);
        assertEquals(delegate.format(formatStr, time), String.format(formatStr, time));
    }

    public void testEquals() throws Throwable {
        String test = "abc";
        MethodDelegate mc1 = MethodDelegate.create(test, "indexOf", IndexOf.class);
        MethodDelegate mc2 = MethodDelegate.create(test, "indexOf", IndexOf.class);
        MethodDelegate mc3 = MethodDelegate.create("other", "indexOf", IndexOf.class);
        MethodDelegate mc4 = MethodDelegate.create(test, "substring", Substring.class);
        MethodDelegate mc5 = MethodDelegate.create(test, "substring", Substring2.class);
        assertTrue(mc1.equals(mc2));
        assertTrue(!mc1.equals(mc3));
        assertTrue(!mc1.equals(mc4));
        assertTrue(mc4.equals(mc5));
    }

    public static interface MainDelegate {
        int main(String[] args);
    }

    public static class MainTest {
        public static int alternateMain(String[] args) {
            return 7;
        }
    }

    public void testStaticDelegate() throws Throwable {
        MainDelegate start = (MainDelegate)MethodDelegate.createStatic(MainTest.class,
                                                                       "alternateMain",
                                                                       MainDelegate.class);
        assertTrue(start.main(null) == 7);
    }

    public static interface Listener {
        public void onEvent();
    }

    public static class Publisher {
        public int test = 0;
        private MulticastDelegate event = MulticastDelegate.create(Listener.class);
        public void addListener(Listener listener) {
            event = event.add(listener);
        }
        public void removeListener(Listener listener) {
            event = event.remove(listener);
        }
        public void fireEvent() {
            ((Listener)event).onEvent();
        }
    }

    public void testPublisher() throws Throwable {
        final Publisher p = new Publisher();
        Listener l1 = new Listener() {
                public void onEvent() {
                    p.test++;
                }
            };
        p.addListener(l1);
        p.addListener(l1);
        p.fireEvent();
        assertTrue(p.test == 2);
        p.removeListener(l1);
        p.fireEvent();
        assertTrue(p.test == 3);
    }

    public static interface SuperSimple {
        public int execute();
    }

    public void testMulticastReturnValue() throws Throwable {
        SuperSimple ss1 = new SuperSimple() {
                public int execute() {
                    return 1;
                }
            };
        SuperSimple ss2 = new SuperSimple() {
                public int execute() {
                    return 2;
                }
            };
        MulticastDelegate multi = MulticastDelegate.create(SuperSimple.class);
        multi = multi.add(ss1);
        multi = multi.add(ss2);
        assertTrue(((SuperSimple)multi).execute() == 2);
        multi = multi.remove(ss1);
        multi = multi.add(ss1);
        assertTrue(((SuperSimple)multi).execute() == 1);
    }

    public TestDelegates(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(TestDelegates.class);
    }

    public void perform(ClassLoader loader) throws Throwable {
    }

    public void testFailOnMemoryLeak() throws Throwable {
    }

}
