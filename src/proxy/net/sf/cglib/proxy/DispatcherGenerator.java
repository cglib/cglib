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
package net.sf.cglib.proxy;

import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.Type;

class DispatcherGenerator implements CallbackGenerator {
    public static final DispatcherGenerator INSTANCE = new DispatcherGenerator();

    private static final Type DISPATCHER =
      TypeUtils.parseType("net.sf.cglib.proxy.Dispatcher");
    private static final Signature LOAD_OBJECT =
      TypeUtils.parseSignature("Object loadObject()");

    public void generate(ClassEmitter ce, final Context context) {
        for (Iterator it = context.getMethods(); it.hasNext();) {
            MethodInfo method = (MethodInfo)it.next();
            if (TypeUtils.isProtected(method.getModifiers())) {
                // ignore protected methods
            } else {
                CodeEmitter e = EmitUtils.begin_method(ce, method);
                context.emitCallback(e, context.getIndex(method));
                e.invoke_interface(DISPATCHER, LOAD_OBJECT);
                e.checkcast(method.getClassInfo().getType());
                e.load_args();
                e.invoke(method);
                e.return_value();
                e.end_method();
            }
        }
    }

    public void generateStatic(CodeEmitter e, Context context) { }
}
