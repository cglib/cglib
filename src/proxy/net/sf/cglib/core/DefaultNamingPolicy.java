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
package net.sf.cglib.core;

import java.util.Set;

/**
 * The default policy used by {@link AbstractClassGenerator}.
 * Generates names such as
 * <p><code>net.sf.cglib.Foo$$EnhancerByCGLIB$$38272841</code><p>
 * This is composed of a prefix based on the name of the superclass, a fixed
 * string incorporating the CGLIB class responsible for generation, and a
 * hashcode derived from the parameters used to create the object. If the same
 * name has been previously been used in the same <code>ClassLoader</code>, a
 * suffix is added to ensure uniqueness.
 */
public class DefaultNamingPolicy implements NamingPolicy {
    public static final DefaultNamingPolicy INSTANCE = new DefaultNamingPolicy();
    
    public String getClassName(String prefix, String source, Object key, Predicate names) {
        
        
        StringBuffer sb = new StringBuffer();
        sb.append( 
                  (prefix != null) ? 
                                     ( 
                                      prefix.startsWith("java") ? 
                                                   "$" + prefix : prefix 
                                     )
                                    : "net.sf.cglib.empty.Object"
                 );
        sb.append("$$");
        sb.append(source.substring(source.lastIndexOf('.') + 1));
        sb.append("ByCGLIB$$");
        sb.append(Integer.toHexString(key.hashCode()));
        String base = sb.toString();
        String attempt = base;
        int index = 2;
        while (names.evaluate(attempt)) {
            attempt = base + "_" + index++;
        }
       
        return attempt;
    }
}
