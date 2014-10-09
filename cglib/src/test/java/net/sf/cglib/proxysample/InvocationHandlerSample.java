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
package net.sf.cglib.proxysample;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.InvocationHandler;

/**
 * @author neeme
 *
 */
public class InvocationHandlerSample implements InvocationHandler {

    private Object o;

    /**
     * Constructor for InvocationHandlerSample.
     */
    public InvocationHandlerSample(Object o) {
        this.o = o;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        System.out.println("invoke() start");
        System.out.println("    method: " + method.getName());
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("    arg: " + args[i]);
            }
        }
        Object r = method.invoke(o, args);
        System.out.println("    return: " + r);
        System.out.println("invoke() end");
        return r;
    }

}
