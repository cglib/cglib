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
package net.sf.cglib.util;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Extends BasicCodeGenerator to provide higher-level generation functions
 * @author Juozas Baliuka, Chris Nokleberg
 */
abstract public class CodeGenerator extends BasicCodeGenerator {
    private static final String FIND_CLASS = "CGLIB$findClass";
    private static final Map primitiveMethods = new HashMap();
    private static final Map primitiveToWrapper = new HashMap();

    private boolean needsFindClass;

    static {
        primitiveMethods.put(Boolean.TYPE, MethodConstants.BOOLEAN_VALUE);
        primitiveMethods.put(Character.TYPE, MethodConstants.CHAR_VALUE);
        primitiveMethods.put(Long.TYPE, MethodConstants.LONG_VALUE);
        primitiveMethods.put(Double.TYPE, MethodConstants.DOUBLE_VALUE);
        primitiveMethods.put(Float.TYPE, MethodConstants.FLOAT_VALUE);
        primitiveMethods.put(Short.TYPE, MethodConstants.INT_VALUE);
        primitiveMethods.put(Integer.TYPE, MethodConstants.INT_VALUE);
        primitiveMethods.put(Byte.TYPE, MethodConstants.INT_VALUE);

        primitiveToWrapper.put(Boolean.TYPE, Boolean.class);
        primitiveToWrapper.put(Character.TYPE, Character.class);
        primitiveToWrapper.put(Long.TYPE, Long.class);
        primitiveToWrapper.put(Double.TYPE, Double.class);
        primitiveToWrapper.put(Float.TYPE, Float.class);
        primitiveToWrapper.put(Short.TYPE, Short.class);
        primitiveToWrapper.put(Integer.TYPE, Integer.class);
        primitiveToWrapper.put(Byte.TYPE, Byte.class);
    }

    protected CodeGenerator(String className, Class superclass, ClassLoader classLoader) {
        super(className, superclass, classLoader);
    }

    protected CodeGenerator() {
    }

    protected void postGenerate() {
        if (needsFindClass) {
            generateFindClass();
        }
    }
    
    protected void load_class_this() {
        load_class_helper(getClassName());
    }
    
    protected void load_class(Class type) {
        if (type.isPrimitive()) {
            if (type.equals(Void.TYPE)) {
                throw new IllegalArgumentException("cannot load void type");
            }
            try {
                getfield(((Class)primitiveToWrapper.get(type)).getDeclaredField("TYPE"));
            } catch (NoSuchFieldException e) {
                throw new CodeGenerationException(e);
            }
        } else {
            load_class_helper(type.getName());
        }
    }
    
    private void load_class_helper(String className) {
        needsFindClass = true;
        push(className);
        invoke_static_this(FIND_CLASS, Class.class, Constants.TYPES_STRING);
    }

    /**
     * Allocates and fills an Object[] array with the arguments to the
     * current method. Primitive values are inserted as their boxed
     * (Object) equivalents.
     */
    protected void create_arg_array() {
        /* generates:
           Object[] args = new Object[]{ arg1, new Integer(arg2) };
         */
        Class[] parameterTypes = getParameterTypes();
        push(parameterTypes.length);
        newarray();
        for (int i = 0; i < parameterTypes.length; i++) {
            dup();
            push(i);
            load_arg(i);
            box(parameterTypes[i]);
            aastore();
        }
    }
    
    protected void push(Object[] array) {
        push(array.length);
        newarray(array.getClass().getComponentType());
        for (int i = 0; i < array.length; i++) {
            dup();
            push(i);
            push_object(array[i]);
            aastore();
        }
    }
    
    protected void push_object(Object obj) {
        if (obj == null) {
            aconst_null();
        } else {
            Class type = obj.getClass();
            if (type.isArray()) {
                push((Object[])obj);
            } else if (obj instanceof String) {
                push((String)obj);
            } else if (obj instanceof Class) {
                load_class((Class)obj);
            } else if (obj.getClass().getSuperclass().equals(Number.class)) {
                throw new IllegalArgumentException("not implemented yet");
            } else {
                throw new IllegalArgumentException("unknown type: " + obj.getClass());
            }
        }
    }
    
    /**
     * Pushes a zero onto the stack if the argument is a primitive class, or a null otherwise.
     */
    protected void zero_or_null(Class type) {
        if (type.isPrimitive()) {
            if (type.equals(Double.TYPE)) {
                push(0d);
            } else if (type.equals(Long.TYPE)) {
                push(0L);
            } else if (type.equals(Float.TYPE)) {
                push(0f);
            } else if (type.equals(Void.TYPE)) {
                // ignore
            } else {
                push(0);
            }
        } else {
            aconst_null();
        }
    }
    
    /**
     * Unboxes the object on the top of the stack. If the object is null, the
     * unboxed primitive value becomes zero.
     */
    protected void unbox_or_zero(Class type) {
        if (type.isPrimitive()) {
            if (!type.equals(Void.TYPE)) {
                Label nonNull = make_label();
                Label end = make_label();
                dup();
                ifnonnull(nonNull);
                pop();
                zero_or_null(type);
                goTo(end);
                mark(nonNull);
                unbox(type);
                mark(end);
            }
        } else {
            checkcast(type);
        }
    }
    
    /**
     * If the argument is a primitive class, replaces the primitive value
     * on the top of the stack with the wrapped (Object) equivalent. For
     * example, char -> Character.
     * If the class is Void, a null is pushed onto the stack instead.
     * @param type the class indicating the current type of the top stack value
     */
    protected void box(Class type) {
        if (type.isPrimitive()) {
            if (type.equals(Void.TYPE)) {
                aconst_null();
            } else {
                Class wrapper = (Class)primitiveToWrapper.get(type);
                new_instance(wrapper);
                if (getStackSize(type) == 2) {
                    // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
                    dup_x2();
                    dup_x2();
                    pop();
                } else {
                    // p -> po -> opo -> oop -> o
                    dup_x1();
                    swap();
                }
                invoke_constructor(wrapper, new Class[]{ type });
            }
        }
    }
    
    /**
     * If the argument is a primitive class, replaces the object
     * on the top of the stack with the unwrapped (primitive)
     * equivalent. For example, Character -> char.
     * @param type the class indicating the desired type of the top stack value
     * @return true if the value was unboxed
     */
    protected void unbox(Class type) {
        if (type.isPrimitive()) {
            if (!type.equals(Void.TYPE)) {
                Method convert = (Method)primitiveMethods.get(type);
                checkcast(convert.getDeclaringClass());
                invoke(convert);
            }
        } else {
            checkcast(type);
        }
    }
    
    protected void null_constructor() {
        begin_constructor();
        load_this();
        super_invoke_constructor();
        return_value();
        end_method();
    }
    
    private void generateFindClass() {
        /* generates:
           static private Class findClass(String name) throws Exception {
               try {
                   return Class.forName(name);
               } catch (java.lang.ClassNotFoundException cne) {
                   throw new java.lang.NoClassDefFoundError(cne.getMessage());
               }
           }
         */
        begin_method(Modifier.PRIVATE | Modifier.STATIC,
        Class.class,
        FIND_CLASS,
        Constants.TYPES_STRING,
        null);

        Block block = begin_block();
        load_this();
        invoke(MethodConstants.FOR_NAME);
        return_value();
        end_block();
        
        catch_exception(block, ClassNotFoundException.class);
        invoke(MethodConstants.THROWABLE_GET_MESSAGE);
        new_instance(NoClassDefFoundError.class);
        dup_x1();
        swap();
        invoke_constructor(NoClassDefFoundError.class, Constants.TYPES_STRING);
        athrow();
        end_method();
    }
    
    public interface ProcessArrayCallback {
        public void processElement(Class type);
    }

    /**
     * Process an array on the stack. Assumes the top item on the stack
     * is an array of the specified type. For each element in the array,
     * puts the element on the stack and triggers the callback.
     * @param type the type of the array (type.isArray() must be true)
     * @param callback the callback triggered for each element
     */
    protected void process_array(Class type, ProcessArrayCallback callback) {
        Class compType = type.getComponentType();
        Local array = make_local();
        Local loopvar = make_local(Integer.TYPE);
        Label loopbody = make_label();
        Label checkloop = make_label();
        store_local(array);
        push(0);
        store_local(loopvar);
        goTo(checkloop);
        
        mark(loopbody);
        load_local(array);
        load_local(loopvar);
        array_load(compType);
        callback.processElement(compType);
        iinc(loopvar, 1);
        
        mark(checkloop);
        load_local(loopvar);
        load_local(array);
        arraylength();
        if_icmplt(loopbody);
    }
    
    /**
     * Process two arrays on the stack in parallel. Assumes the top two items on the stack
     * are arrays of the specified class. The arrays must be the same length. For each pair
     * of elements in the arrays, puts the pair on the stack and triggers the callback.
     * @param type the type of the arrays (type.isArray() must be true)
     * @param callback the callback triggered for each pair of elements
     */
    protected void process_arrays(Class type, ProcessArrayCallback callback) {
        Class compType = type.getComponentType();
        Local array1 = make_local();
        Local array2 = make_local();
        Local loopvar = make_local(Integer.TYPE);
        Label loopbody = make_label();
        Label checkloop = make_label();
        store_local(array1);
        store_local(array2);
        push(0);
        store_local(loopvar);
        goTo(checkloop);
        
        mark(loopbody);
        load_local(array1);
        load_local(loopvar);
        array_load(compType);
        load_local(array2);
        load_local(loopvar);
        array_load(compType);
        callback.processElement(compType);
        iinc(loopvar, 1);
        
        mark(checkloop);
        load_local(loopvar);
        load_local(array1);
        arraylength();
        if_icmplt(loopbody);
    }
    
    /**
     * Branches to the specified label if the top two items on the stack
     * are not equal. The items must both be of the specified
     * class. Equality is determined by comparing primitive values
     * directly and by invoking the <code>equals</code> method for
     * Objects. Arrays are recursively processed in the same manner.
     */
    protected void not_equals(Class type, final Label notEquals) {
        (new ProcessArrayCallback() {
            public void processElement(Class type) {
                not_equals_helper(type, notEquals, this);
            }
        }).processElement(type);
    }
    
    private void not_equals_helper(Class type, Label notEquals, ProcessArrayCallback callback) {
        if (type.isPrimitive()) {
            Class returnType = getReturnType();
            if (returnType.equals(Double.TYPE)) {
                dcmpg();
                ifne(notEquals);
            } else if (returnType.equals(Long.TYPE)) {
                lcmp();
                ifne(notEquals);
            } else if (returnType.equals(Float.TYPE)) {
                fcmpg();
                ifne(notEquals);
            } else {
                if_icmpne(notEquals);
            }
        } else {
            Label end = make_label();
            nullcmp(notEquals, end);
            if (type.isArray()) {
                Label checkContents = make_label();
                dup2();
                arraylength();
                swap();
                arraylength();
                if_icmpeq(checkContents);
                pop2();
                goTo(notEquals);
                mark(checkContents);
                process_arrays(type, callback);
            } else {
                invoke(MethodConstants.EQUALS);
                ifeq(notEquals);
            }
            mark(end);
        }
    }
    
    protected void throw_exception(Class type, String msg) {
        new_instance(type);
        dup();
        push(msg);
        invoke_constructor(type, new Class[]{ String.class });
        athrow();
        
    }
    
    ///// TODO: get rid of this
    /**
     * If both objects on the top of the stack are non-null, does nothing.
     * If one is null, or both are null, both are popped off and execution
     * branches to the respective label.
     * @param oneNull label to branch to if only one of the objects is null
     * @param bothNull label to branch to if both of the objects are null
     */
    protected void nullcmp(Label oneNull, Label bothNull) {
        dup2();
        Label nonNull = make_label();
        Label oneNullHelper = make_label();
        Label end = make_label();
        ifnonnull(nonNull);
        ifnonnull(oneNullHelper);
        pop2();
        goTo(bothNull);
        
        mark(nonNull);
        ifnull(oneNullHelper);
        goTo(end);
        
        mark(oneNullHelper);
        pop2();
        goTo(oneNull);
        
        mark(end);
    }
    
    protected void factory_method(Method method) {
        begin_method(method);
        new_instance_this();
        dup();
        load_args();
        invoke_constructor_this(method.getParameterTypes());
        return_value();
        end_method();
    }

    public interface ProcessSwitchCallback {
        void processCase(int index, Label end);
        void processDefault();
    }

    // TODO: determine what value javac uses
    private static final float MIN_TABLESWITCH_DENSITY = 0.5f;

    // TODO: provide switch capabilities in BasicCodeGenerator?
    protected void process_switch(int[] keys, ProcessSwitchCallback callback) {
        // TODO: should we make a copy?
        Arrays.sort(keys);
        int len = keys.length;
        int min = keys[0];
        int max = keys[len - 1];
        int range = max - min + 1;
        float density = (float)len / range;
        boolean useTable = density >= MIN_TABLESWITCH_DENSITY;

        Label start = make_label();
        Label def = make_label();
        Label end = make_label();
        goTo(start);

        Label[] labels;
        if (useTable) {
            labels = new Label[range];
            Arrays.fill(labels, def);
            for (int i = 0; i < len; i++) {
                labels[keys[i] - min] = make_label();
            }
            for (int i = 0; i < range; i++) {
                Label label = labels[i];
                if (label != def) {
                    mark(label);
                    callback.processCase(i + min, end);
                }
            }
        } else {
            labels = new Label[len];
            for (int i = 0; i < len; i++) {
                mark(labels[i] = make_label());
                callback.processCase(keys[i], end);
            }
        }

        mark(def);
        callback.processDefault();
        goTo(end);
        mark(start);
        if (useTable) {
            getBackend().emit_switch(min, max, labels, def);
        } else {
            getBackend().emit_switch(keys, labels, def);
        }
        mark(end);
    }
}
