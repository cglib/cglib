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
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

/**
 * @author Chris Nokleberg
 * @version $Id: MixinEmitter.java,v 1.7 2003/09/20 09:22:22 herbyderby Exp $
 */
class MixinEmitter extends Emitter {
    private static final String FIELD_NAME = "CGLIB$DELEGATES";
    private static final Signature NEW_INSTANCE =
      Signature.parse("net.sf.cglib.Mixin newInstance(Object[])");

    public MixinEmitter(ClassVisitor v, String className, Class[] classes, int[] route) {
        super(v);

        Ops.begin_class(this,
                        Constants.ACC_PUBLIC,
                        className,
                        Mixin.class,
                        classes,
                        Constants.SOURCE_FILE);

        null_constructor();
        factory_method(NEW_INSTANCE);

        declare_field(Constants.ACC_PRIVATE, FIELD_NAME, Types.OBJECT_ARRAY, null);
        begin_method(Constants.ACC_PUBLIC, Signatures.CSTRUCT_OBJECT_ARRAY, null);
        load_this();
        super_invoke_constructor();
        load_this();
        load_arg(0);
        putfield(FIELD_NAME);
        return_value();

        Set unique = new HashSet();
        for (int i = 0; i < classes.length; i++) {
            Method[] methods = classes[i].getMethods();
            for (int j = 0; j < methods.length; j++) {
                if (unique.add(MethodWrapper.create(methods[j]))) {
                    Method method = methods[j];
                    Ops.begin_method(this, method);
                    load_this();
                    getfield(FIELD_NAME);
                    aaload((route != null) ? route[i] : i);
                    checkcast(Type.getType(method.getDeclaringClass()));
                    load_args();
                    Ops.invoke(this, method);
                    return_value();
                }
            }
        }

        end_class();
    }
}
