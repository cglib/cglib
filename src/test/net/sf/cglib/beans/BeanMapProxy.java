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
package net.sf.cglib.beans;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: BeanMapProxy.java,v 1.2 2004/06/24 21:15:17 herbyderby Exp $
 */
public class BeanMapProxy implements InvocationHandler {
    private Map map;

    public static Object newInstance(Map map, Class[] interfaces) {
        return Proxy.newProxyInstance(map.getClass().getClassLoader(),
                                      interfaces,
                                      new BeanMapProxy(map));
    }

    public BeanMapProxy(Map map) {
        this.map = map;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        String name = m.getName();
        if (name.startsWith("get")) {
            return map.get(name.substring(3));
        } else if (name.startsWith("set")) {
            map.put(name.substring(3), args[0]);
            return null;
        }
        return null;
    }
}

