/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
package net.sf.cglib.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

class ParallelSorterEmitter extends Emitter {
    private static final Signature NEW_INSTANCE =
      Signature.parse("net.sf.cglib.util.ParallelSorter newInstance(Object[])");
    private static final Signature SWAP =
      Signature.parse("void swap(int, int)");

    public ParallelSorterEmitter(ClassVisitor v, String className, Object[] arrays) throws Exception {
        super(v);
        Ops.begin_class(this, Modifier.PUBLIC, className, ParallelSorter.class, null, Constants.SOURCE_FILE);
        Ops.null_constructor(this);
        Ops.factory_method(this, NEW_INSTANCE);
        generateConstructor(arrays);
        generateSwap(arrays);
        end_class();
    }

    private String getFieldName(int index) {
        return "FIELD_" + index;
    }

    private void generateConstructor(Object[] arrays) throws NoSuchFieldException {
        begin_method(Constants.ACC_PUBLIC, Signatures.CSTRUCT_OBJECT_ARRAY, null);
        load_this();
        super_invoke_constructor();
        load_this();
        load_arg(0);
        super_putfield("a", Types.OBJECT_ARRAY);
        for (int i = 0; i < arrays.length; i++) {
            Type type = Type.getType(arrays[i].getClass());
            declare_field(Modifier.PRIVATE, getFieldName(i), type, null);
            load_this();
            load_arg(0);
            push(i);
            aaload();
            checkcast(type);
            putfield(getFieldName(i));
        }
        return_value();
    }

    private void generateSwap(Object[] arrays) {
        begin_method(Constants.ACC_PUBLIC, SWAP, null);
        for (int i = 0; i < arrays.length; i++) {
            Type type = Type.getType(arrays[i].getClass());
            Type component = Emitter.getComponentType(type);
            Local T = make_local(type);

            load_this();
            getfield(getFieldName(i));
            store_local(T);

            load_local(T);
            load_arg(0);

            load_local(T);
            load_arg(1);
            array_load(component);
                
            load_local(T);
            load_arg(1);

            load_local(T);
            load_arg(0);
            array_load(component);

            array_store(component);
            array_store(component);
        }
        return_value();
    }
}
