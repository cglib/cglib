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

public class TestDelegator extends TestCase {    
    public void testSimple() throws Exception {
        Object obj = Delegator.makeDelegator(new Class[]{ DI1.class, DI2.class },
                                             new Object[]{ new D1(), new D2() },
                                             null);
        assertTrue(((DI1)obj).herby().equals("D1"));
        assertTrue(((DI2)obj).derby().equals("D2"));
    }

    public void testDetermineInterfaces() throws Exception {
        Object obj = Delegator.makeDelegator(new Object[]{ new D1(), new D2() }, null);
        assertTrue(((DI1)obj).herby().equals("D1"));
        assertTrue(((DI2)obj).derby().equals("D2"));
    }
 
    public void testOverride() throws Exception {
        Object obj = Delegator.makeDelegator(new Object[]{ new D1(), new D4() }, null);
        assertTrue(((DI1)obj).herby().equals("D1"));
        assertTrue(((DI2)obj).derby().equals("D4"));
    }

    public void testNonOverride() throws Exception {
        Object obj = Delegator.makeDelegator(new Object[]{ new D4(), new D1() }, null);
        assertTrue(((DI1)obj).herby().equals("D4"));
        assertTrue(((DI2)obj).derby().equals("D4"));
    }

    public void testSubclass() throws Exception {
        Object obj = Delegator.makeDelegator(new Object[]{ new D3(), new D1() }, null);
        assertTrue(((DI1)obj).herby().equals("D1"));
        assertTrue(((DI2)obj).derby().equals("D2"));
        assertTrue(((DI3)obj).extra().equals("D3"));
    }

    public TestDelegator(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestDelegator.class);
    }
}
