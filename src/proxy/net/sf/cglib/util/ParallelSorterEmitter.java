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

class ParallelSorterEmitter extends ClassEmitter {
    private static final Signature CSTRUCT_OBJECT_ARRAY =
      TypeUtils.parseConstructor("Object[]");
    private static final Signature NEW_INSTANCE =
      TypeUtils.parseSignature("net.sf.cglib.util.ParallelSorter newInstance(Object[])");
    private static final Signature SWAP =
      TypeUtils.parseSignature("void swap(int, int)");
    private static final Type PARALLEL_SORTER =
      TypeUtils.parseType("net.sf.cglib.util.ParallelSorter");

    public ParallelSorterEmitter(ClassVisitor v, String className, Object[] arrays) {
        super(v);
        begin_class(Constants.ACC_PUBLIC, className, PARALLEL_SORTER, null, Constants.SOURCE_FILE);
        EmitUtils.null_constructor(this);
        EmitUtils.factory_method(this, NEW_INSTANCE);
        generateConstructor(arrays);
        generateSwap(arrays);
        end_class();
    }

    private String getFieldName(int index) {
        return "FIELD_" + index;
    }

    private void generateConstructor(Object[] arrays) {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, CSTRUCT_OBJECT_ARRAY, null);
        e.load_this();
        e.super_invoke_constructor();
        e.load_this();
        e.load_arg(0);
        e.super_putfield("a", Constants.TYPE_OBJECT_ARRAY);
        for (int i = 0; i < arrays.length; i++) {
            Type type = Type.getType(arrays[i].getClass());
            declare_field(Constants.ACC_PRIVATE, getFieldName(i), type, null);
            e.load_this();
            e.load_arg(0);
            e.push(i);
            e.aaload();
            e.checkcast(type);
            e.putfield(getFieldName(i));
        }
        e.return_value();
        e.end_method();
    }

    private void generateSwap(final Object[] arrays) {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, SWAP, null);
        for (int i = 0; i < arrays.length; i++) {
            Type type = Type.getType(arrays[i].getClass());
            Type component = TypeUtils.getComponentType(type);
            Local T = e.make_local(type);

            e.load_this();
            e.getfield(getFieldName(i));
            e.store_local(T);

            e.load_local(T);
            e.load_arg(0);

            e.load_local(T);
            e.load_arg(1);
            e.array_load(component);
                
            e.load_local(T);
            e.load_arg(1);

            e.load_local(T);
            e.load_arg(0);
            e.array_load(component);

            e.array_store(component);
            e.array_store(component);
        }
        e.return_value();
        e.end_method();
    }
}
