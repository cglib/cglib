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

import junit.framework.*;
import java.util.*;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: TestKeyFactory.java,v 1.8 2003/01/05 23:55:55 herbyderby Exp $
 */
public class TestKeyFactory extends CodeGenTestCase {
    public interface MyKey {
        public Object newInstance(int a, int[] b, boolean flag);
    }

    public interface MyKey2 {
        public Object newInstance(int[][] a);
    }

    public interface CharArrayKey {
        public Object newInstance(char[] a);
    }    

    public interface BooleanArrayKey {
        public Object newInstance(boolean[] a);
    }    

    public interface MethodKey {
        public Object newInstance(Class returnType, Class[] parameterTypes);
    }

    public void testSimple() throws Exception {
        MyKey mykey = (MyKey)KeyFactory.create(MyKey.class, null);
        assertTrue(mykey.newInstance(5, new int[]{ 6, 7 }, false).hashCode() ==
                   mykey.newInstance(5, new int[]{ 6, 7 }, false).hashCode());
    }

    public void testNested() throws Exception {
        MyKey2 mykey2 = (MyKey2)KeyFactory.create(MyKey2.class, null);
        Object instance = mykey2.newInstance(new int[][]{ { 1, 2 }, { 3, 4 } });
        int code1 = instance.hashCode();
        int result = ((KeyFactory)instance).getHashConstant();
        int mult = ((KeyFactory)instance).getHashMultiplier();
        int code2 = ((((result + 1) * mult + 2) * mult + 3) * mult + 4) * mult;
        assertTrue(code1 == code2);
    }

    public void testCharArray() throws Exception {
        CharArrayKey f = (CharArrayKey)KeyFactory.create(CharArrayKey.class, null);
        Object key1 = f.newInstance(new char[]{ 'a', 'b' });
        Object key2 = f.newInstance(new char[]{ 'a', '_' });
        assertTrue(!key1.equals(key2));
        assertTrue(((char[])((Object[])((KeyFactory)key2).getArgs())[0])[1] == '_');
    }

    public void testBooleanArray() throws Exception {
        BooleanArrayKey f = (BooleanArrayKey)KeyFactory.create(BooleanArrayKey.class, null);
        Object key1 = f.newInstance(new boolean[]{ true, false, true });
        Object key2 = f.newInstance(new boolean[]{ true, false, true });
        assertTrue(key1.equals(key2));
    }

    public void testMethodKey() throws Exception {
        MethodKey factory = (MethodKey)KeyFactory.create(MethodKey.class, null);
        Set methodSet = new HashSet();
        methodSet.add(factory.newInstance(Number.class, new Class[]{ int.class }));
        assertTrue(methodSet.contains(factory.newInstance(Number.class, new Class[]{ int.class })));
        assertTrue(!methodSet.contains(factory.newInstance(Number.class, new Class[]{ Integer.class })));
    }

    public void testEqualOtherClass() throws Exception {
        MyKey mykey = (MyKey)KeyFactory.create(MyKey.class, null);
        assertTrue(!mykey.newInstance(5, new int[]{ 6, 7 }, false).equals(new Object()));
    }
    
    public TestKeyFactory(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestKeyFactory.class);
    }
}
