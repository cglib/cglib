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
package net.sf.cglib.core;

import java.lang.reflect.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class ReflectEmitter extends Emitter2 {
    public ReflectEmitter(ClassVisitor v) {
        super(v);
    }

    private static Type[] getTypes(Class[] classes) {
        if (classes == null) {
            return null;
        }
        Type[] types = new Type[classes.length];
        for (int i = 0; i < classes.length; i++) {
            types[i] = Type.getType(classes[i]);
        }
        return types;
    }

    private Type getType(String fqcn) {
        System.err.println("fqcn: " + fqcn);
        return Type.getType(fqcn.replace('.', '/'));
    }

    public void begin_class(int access, String className, Class superclass, Class[] interfaces, String sourceFile) {
        begin_class(access,
                    getType(className),
                    Type.getType(superclass),
                    getTypes(interfaces),
                    sourceFile);
    }

    public void begin_method(int access, String name, Class returnType, Class[] parameterTypes, Class[] exceptions) {
        // TODO: this could be optimized
        begin_method(access,
                     name,
                     Type.getType(returnType),
                     getTypes(parameterTypes),
                     getTypes(exceptions));
    }

    public void begin_constructor(int access, Class[] parameterTypes, Class[] exceptions) {
        begin_constructor(access, getTypes(parameterTypes), getTypes(exceptions));
    }

    public void invoke_virtual_this(String methodName, Class returnType, Class[] parameterTypes) {
        invoke_virtual_this(methodName, Type.getType(returnType), getTypes(parameterTypes));
    }

    public void invoke_static_this(String methodName, Class returnType, Class[] parameterTypes) {
        invoke_static_this(methodName, Type.getType(returnType), getTypes(parameterTypes));
    }
        
//     public void invoke_constructor(String className, Class[] parameterTypes) {
//         invoke_constructor(getType(className), getTypes(parameterTypes));
//     }
        
    public void invoke_constructor(Class type) {
        invoke_constructor(Type.getType(type));
    }
    
    public void invoke_constructor(Class type, Class[] parameterTypes) {
        invoke_constructor(Type.getType(type), getTypes(parameterTypes));
    }

    public void super_invoke_constructor(Class[] parameterTypes) {
        super_invoke_constructor(getTypes(parameterTypes));
    }
    
    public void invoke_constructor_this(Class[] parameterTypes) {
        invoke_constructor_this(getTypes(parameterTypes));
    }

    public void begin_method(Method method) {
        begin_method(method, getDefaultModifiers(method.getModifiers()));
    }
    
    public void begin_method(Method method, int modifiers) {
        begin_method(modifiers,
                     method.getName(),
                     method.getReturnType(), 
                     method.getParameterTypes(),
                     method.getExceptionTypes());
    }

    public static int getDefaultModifiers(int modifiers) {
        return Modifier.FINAL
            | (modifiers
               & ~Modifier.ABSTRACT
               & ~Modifier.NATIVE
               & ~Modifier.SYNCHRONIZED);
    }

    public void begin_constructor(Constructor constructor) {
        begin_constructor(constructor.getModifiers(),
                          constructor.getParameterTypes(),
                          constructor.getExceptionTypes());
    }
    
    public void getfield(Field field) {
        int opcode = Modifier.isStatic(field.getModifiers()) ? Constants.GETSTATIC : Constants.GETFIELD;
        fieldHelper(opcode, field);
    }
    
    public void putfield(Field field) {
        int opcode = Modifier.isStatic(field.getModifiers()) ? Constants.PUTSTATIC : Constants.PUTFIELD;
        fieldHelper(opcode, field);
    }

    private void fieldHelper(int opcode, Field field) {
        // TODO: remove need for direct access to emit_field?
        emit_field(opcode,
                   Type.getType(field.getDeclaringClass()),
                   field.getName(),
                   Type.getType(field.getType()));
    }

    public void invoke(Method method) {
        int opcode;
        if (method.getDeclaringClass().isInterface()) {
            opcode = Constants.INVOKEINTERFACE;
        } else if (Modifier.isStatic(method.getModifiers())) {
            opcode = Constants.INVOKESTATIC;
        } else {
            opcode = Constants.INVOKEVIRTUAL;
        }
        emit_invoke(opcode,
                    Type.getType(method.getDeclaringClass()),
                    method.getName(),
                    Type.getType(method.getReturnType()),
                    getTypes(method.getParameterTypes()));
    }
    
    public void super_invoke(Method method) {
        // TODO: remove need for direct access to emit_invoke?
        emit_invoke(Constants.INVOKESPECIAL,
                    getSuperType(),
                    method.getName(),
                    Type.getType(method.getReturnType()),
                    getTypes(method.getParameterTypes()));
    }

    public void invoke(Constructor constructor) {
        invoke_constructor(constructor.getDeclaringClass(), constructor.getParameterTypes());
    }
    
    public void super_invoke(Constructor constructor) {
        super_invoke_constructor(constructor.getParameterTypes());
    }

    
    ////////////////////////////// MAYBE REMOVE THESE //////////////////////////////
    
    public void math(int op, Class type) {
        math(op, Type.getType(type));
    }

    public void catch_exception(Block2 block, Class exception) {
        catch_exception(block, Type.getType(exception));
    }
    
    public void if_cmpeq(Class type, org.objectweb.asm.Label label) {
        if_cmpeq(Type.getType(type), label);
    }

    public void if_cmpne(Class type, org.objectweb.asm.Label label) {
        if_cmpne(Type.getType(type), label);
    }

    public void if_cmplt(Class type, org.objectweb.asm.Label label) {
        if_cmplt(Type.getType(type), label);
    }

    public void if_cmpgt(Class type, org.objectweb.asm.Label label) {
        if_cmpgt(Type.getType(type), label);
    }

    public void cast_numeric(Class from, Class to) {
        cast_numeric(Type.getType(from), Type.getType(to));
    }
    
    public void newarray(Class type) {
        newarray(Type.getType(type));
    }
    
    public void array_load(Class type) {
        array_load(Type.getType(type));
    }
    
    public void array_store(Class type) {
        array_store(Type.getType(type));
    }
    
    public void declare_field(int access, String name, Class type, Object value) {
        declare_field(access, name, Type.getType(type), value);
    }
    
    public void super_getfield(String name, Class type) {
        super_getfield(name, Type.getType(type));
    }

    public void super_putfield(String name, Class type) {
        super_putfield(name, Type.getType(type));
    }

    public void super_getstatic(String name, Class type) {
        super_getstatic(name, Type.getType(type));
    }

    public void super_putstatic(String name, Class type) {
        super_putstatic(name, Type.getType(type));
    }

    public void new_instance(Class type) {
        new_instance(Type.getType(type));
    }
        
    public Local2 make_local(Class type) {
        return make_local(Type.getType(type));
    }
    
    public void checkcast(Class type) {
        checkcast(Type.getType(type));
    }
    
    public void instance_of(Class type) {
        instance_of(Type.getType(type));
    }
}
