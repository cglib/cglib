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
package net.sf.cglib.proxy;

import net.sf.cglib.CodeGenTestCase;
import java.beans.*;
import java.lang.reflect.Method;
import java.util.*;
import junit.framework.*;

/**
 * @author Chris Nokleberg
 * @version $Id: TestDispatcher.java,v 1.3 2003/11/11 04:35:13 herbyderby Exp $
 */
public class TestDispatcher extends CodeGenTestCase {
    interface Foo {
        String foo();
    }

    interface Bar {
        String bar();
    }
    
    public void testSimple() throws Exception {
        final Object[] impls = new Object[]{
            new Foo() {
                public String foo() {
                    return "foo1";
                }
            },
            new Bar() {
                public String bar() {
                    return "bar1";
                }
            }
        };

        Callback[] callbacks = new Callback[]{
            new Dispatcher() {
                public Object loadObject() {
                    return impls[0];
                }
            },
            new Dispatcher() {
                public Object loadObject() {
                    return impls[1];
                }
            }
        };

        Enhancer e = new Enhancer();
        e.setInterfaces(new Class[]{ Foo.class, Bar.class });
        e.setCallbacks(callbacks);
        e.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
                return (method.getDeclaringClass().equals(Foo.class)) ? 0 : 1;
            }
        });
        Object obj = e.create();
        
        assertTrue(((Foo)obj).foo().equals("foo1"));
        assertTrue(((Bar)obj).bar().equals("bar1"));

        impls[0] = new Foo() {
            public String foo() {
                return "foo2";
            }
        };
        assertTrue(((Foo)obj).foo().equals("foo2"));
        assertTrue(((Foo)((Factory)obj).newInstance()).foo().equals("foo2"));
    }

    public TestDispatcher(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestDispatcher.class);
    }
}
