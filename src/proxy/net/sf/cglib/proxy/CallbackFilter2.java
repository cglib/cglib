/*
 * Copyright 2004 The Apache Software Foundation
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
package net.sf.cglib.proxy;

import net.sf.cglib.core.MethodInfo;

public interface CallbackFilter2 {
    static final CallbackFilter2 ALL_ZERO = new CallbackFilter2() {
        public int accept(MethodInfo method) {
            return 0;
        }
        public int hashCode() {
            return 999;
        }
    };
    
    /**
     * Map a method to a callback.
     * @param methodInfo the intercepted method
     * @return the index into the array of callbacks (as specified by {@link Enhancer#setCallbacks}) to use for the method, 
     */
    int accept(MethodInfo methodInfo);
}
