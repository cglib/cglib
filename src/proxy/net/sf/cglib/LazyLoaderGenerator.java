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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import net.sf.cglib.core.*;

class LazyLoaderGenerator
implements CallbackGenerator
{
    public static final LazyLoaderGenerator INSTANCE = new LazyLoaderGenerator();

    private static final String DELEGATE = "CGLIB$LAZY_LOADER";
    private static final String LOAD_PRIVATE = "CGLIB$LOAD_PRIVATE";
    private static final Method LOAD_OBJECT =
      ReflectUtils.findMethod("LazyLoader.loadObject()");

    public void generate(Emitter cg, Context context) {
        cg.declare_field(Modifier.PRIVATE, Object.class, DELEGATE);

        cg.begin_method(Modifier.PRIVATE | Modifier.SYNCHRONIZED | Modifier.FINAL,
                        Object.class,
                        LOAD_PRIVATE,
                        null,
                        null);
        cg.load_this();
        cg.getfield(DELEGATE);
        cg.dup();
        Label end = cg.make_label();
        cg.ifnonnull(end);
        cg.pop();
        cg.load_this();
        context.emitCallback();
        cg.checkcast(LazyLoader.class);
        cg.invoke(LOAD_OBJECT);
        cg.dup_x1();
        cg.putfield(DELEGATE);
        cg.mark(end);
        cg.return_value();
        cg.end_method();

        for (Iterator it = context.getMethods(); it.hasNext();) {
            Method method = (Method)it.next();
            if (Modifier.isProtected(method.getModifiers())) {
                // ignore protected methods
            } else {
                cg.begin_method(method, context.getModifiers(method));
                cg.load_this();
                cg.dup();
                cg.invoke_virtual_this(LOAD_PRIVATE, Object.class, null);
                cg.checkcast(method.getDeclaringClass());
                cg.load_args();
                cg.invoke(method);
                cg.return_value();
                cg.end_method();
            }
        }
    }

    public void generateStatic(Emitter cg, Context context) { }
}
