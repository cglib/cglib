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
import java.lang.reflect.Method;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.*;
import java.util.*;
import net.sf.cglib.UndeclaredThrowableException;

/**
 * Extends BasicCodeGenerator to provide higher-level generation functions
 * @author Juozas Baliuka, Chris Nokleberg
 */
abstract public class CodeGenerator extends BasicCodeGenerator {
    public static final int SWITCH_STYLE_TRIE = 0;
    public static final int SWITCH_STYLE_HASH = 1;
    
    private static final String FIND_CLASS = "CGLIB$findClass";
    private static final Map primitiveMethods = new HashMap();

    private static final Class[] TYPES_STRING = { String.class };
    private static final Class[] TYPES_THROWABLE = { Throwable.class };

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
    }

    protected CodeGenerator() {
    }

    protected void postGenerate() {
        if (needsFindClass) {
            generateFindClass();
        }
    }
    
    public void load_class_this() {
        load_class_helper(getClassName());
    }
    
    public void load_class(Class type) {
        if (type.isPrimitive()) {
            if (type.equals(Void.TYPE)) {
                throw new IllegalArgumentException("cannot load void type");
            }
            try {
                getfield(ReflectUtils.getBoxedType(type).getDeclaredField("TYPE"));
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
        invoke_static_this(FIND_CLASS, Class.class, TYPES_STRING);
    }

    /**
     * Allocates and fills an Object[] array with the arguments to the
     * current method. Primitive values are inserted as their boxed
     * (Object) equivalents.
     */
    public void create_arg_array() {
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
    
    public void push(Object[] array) {
        push(array.length);
        newarray(array.getClass().getComponentType());
        for (int i = 0; i < array.length; i++) {
            dup();
            push(i);
            push_object(array[i]);
            aastore();
        }
    }
    
    public void push_object(Object obj) {
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
            } else if (obj instanceof BigInteger) {
                new_instance(BigInteger.class);
                dup();
                push(obj.toString());
                invoke_constructor(BigInteger.class);
            } else if (obj instanceof BigDecimal) {
                new_instance(BigDecimal.class);
                dup();
                push(obj.toString());
                invoke_constructor(BigDecimal.class);
            } else if (obj instanceof Number) {
                push_unboxed(obj);
            } else {
                throw new IllegalArgumentException("unknown type: " + obj.getClass());
            }
        }
    }

    public void push(boolean value) {
        push(value ? 1 : 0);
    }

    /**
     * If the object is a Number, Boolean, or Character, pushes the equivalent primitive
     * value onto the stack. Otherwise, calls push_object(obj).
     */
    public void push_unboxed(Object obj)
    {
        if (obj == null || !ReflectUtils.getUnboxedType(obj.getClass()).isPrimitive()) {
            push_object(obj);
        } else if (obj instanceof Boolean) {
            push(((Boolean)obj).booleanValue() ? 1 : 0);
        } else if (obj instanceof Character) {
            push((short)((Character)obj).charValue());
        } else if (obj instanceof Long) {
            push(((Long)obj).longValue());
        } else if (obj instanceof Double) {
            push(((Double)obj).doubleValue());
        } else if (obj instanceof Float) {
            push(((Float)obj).floatValue());
        } else {
            push(((Number)obj).intValue());
        }
    }
    
    /**
     * Pushes a zero onto the stack if the argument is a primitive class, or a null otherwise.
     */
    public void zero_or_null(Class type) {
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
     * Toggles the integer on the top of the stack from 1 to 0 or vice versa
     */
    public void not() {
        push(1);
        xor(Integer.TYPE);
    }
    
    /**
     * Unboxes the object on the top of the stack. If the object is null, the
     * unboxed primitive value becomes zero.
     */
    public void unbox_or_zero(Class type) {
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
    public void box(Class type) {
        if (type.isPrimitive()) {
            if (type.equals(Void.TYPE)) {
                aconst_null();
            } else {
                Class wrapper = ReflectUtils.getBoxedType(type);
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
    public void unbox(Class type) {
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
    
    public void null_constructor() {
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
        TYPES_STRING,
        null);

        Block block = begin_block();
        load_arg(0);
        invoke(MethodConstants.FOR_NAME);
        return_value();
        end_block();
        
        catch_exception(block, ClassNotFoundException.class);
        invoke(MethodConstants.THROWABLE_GET_MESSAGE);
        new_instance(NoClassDefFoundError.class);
        dup_x1();
        swap();
        invoke_constructor(NoClassDefFoundError.class, TYPES_STRING);
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
    public void process_array(Class type, ProcessArrayCallback callback) {
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
    public void process_arrays(Class type, ProcessArrayCallback callback) {
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
    public void not_equals(Class type, final Label notEquals) {
        (new ProcessArrayCallback() {
            public void processElement(Class type) {
                not_equals_helper(type, notEquals, this);
            }
        }).processElement(type);
    }
    
    private void not_equals_helper(Class type, Label notEquals, ProcessArrayCallback callback) {
        if (type.isPrimitive()) {
            if_cmpne(type, notEquals);
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
    
    public void throw_exception(Class type, String msg) {
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
    public void nullcmp(Label oneNull, Label bothNull) {
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
    
    public void factory_method(Method method) {
        begin_method(method);
        new_instance_this();
        dup();
        load_args();
        invoke_constructor_this(method.getParameterTypes());
        return_value();
        end_method();
    }

    public interface StringSwitchCallback {
        void processCase(String key, Label end) throws Exception;
        void processDefault() throws Exception;
    }

    public void string_switch(String[] strings, int switchStyle, StringSwitchCallback callback)
    throws Exception {
        switch (switchStyle) {
        case SWITCH_STYLE_TRIE:
            string_switch_trie(strings, callback);
            break;
        case SWITCH_STYLE_HASH:
            string_switch_hash(strings, callback);
            break;
        default:
            throw new IllegalArgumentException("unknown switch style " + switchStyle);
        }
    }

    private void string_switch_trie(String[] strings, final StringSwitchCallback callback) throws Exception {
        final Label def = make_label();
        final Label end = make_label();
        final Map buckets = bucketByLength(Arrays.asList(strings));
        dup();
        invoke(MethodConstants.STRING_LENGTH);
        process_switch(getSwitchKeys(buckets), new ProcessSwitchCallback() {
                public void processCase(int key, Label ignore_end) throws Exception {
                    List bucket = (List)buckets.get(new Integer(key));
                    stringSwitchHelper(bucket, callback, def, end, 0);
                }
                public void processDefault() {
                    goTo(def);
                }
            });
        mark(def);
        pop();
        callback.processDefault();
        mark(end);
    }

    private void stringSwitchHelper(List strings,
                                    final StringSwitchCallback callback,
                                    final Label def,
                                    final Label end,
                                    final int index) throws Exception {
        final int len = ((String)strings.get(0)).length();
        final Map buckets = bucketByChar(strings, index);
        dup();
        push(index);
        invoke(MethodConstants.STRING_CHAR_AT);
        process_switch(getSwitchKeys(buckets), new ProcessSwitchCallback() {
                public void processCase(int key, Label ignore_end) throws Exception {
                    List bucket = (List)buckets.get(new Integer(key));
                    if (index + 1 == len) {
                        pop();
                        callback.processCase((String)bucket.get(0), end);
                    } else {
                        stringSwitchHelper(bucket, callback, def, end, index + 1);
                    }
                }
                public void processDefault() {
                    goTo(def);
                }
            });
    }        

    private interface Indexer {
        int index(String value);
    }

    private static Map bucketByLength(List strings) {
        return bucketHelper(strings, new Indexer() {
                public int index(String value) {
                    return value.length();
                }
            });
    }

    private static Map bucketByChar(List strings, final int index) {
        return bucketHelper(strings, new Indexer() {
                public int index(String value) {
                    return value.charAt(index);
                }
            });
    }

    private static Map bucketHelper(List strings, Indexer t) {
        Map buckets = new HashMap();
        for (Iterator it = strings.iterator(); it.hasNext();) {
            String value = (String)it.next();
            Integer key = new Integer(t.index(value));
            List bucket = (List)buckets.get(key);
            if (bucket == null) {
                buckets.put(key, bucket = new LinkedList());
            }
            bucket.add(value);
        }
        return buckets;
    }

    private static int[] getSwitchKeys(Map buckets) {
        int[] keys = new int[buckets.size()];
        int index = 0;
        for (Iterator it = buckets.keySet().iterator(); it.hasNext();) {
            keys[index++] = ((Integer)it.next()).intValue();
        }
        Arrays.sort(keys);
        return keys;
    }

    private void string_switch_hash(final String[] strings,
                                    final StringSwitchCallback callback) throws Exception {
        final Label def = make_label();
        final Label end = make_label();
        int[] hashCodes = new int[strings.length];
        final Map indexes = new HashMap();
        for (int i = 0; i < strings.length; i++) {
            hashCodes[i] = strings[i].hashCode();
            Integer key = new Integer(hashCodes[i]);
            if (indexes.containsKey(key)) {
                // TODO: allow duplicate hash codes, differentiate using == in leaves
                throw new IllegalArgumentException("strings have duplicate hash codes");
            }
            indexes.put(key, new Integer(i));
        }
        Arrays.sort(hashCodes);

        dup();
        invoke(MethodConstants.HASH_CODE);
        process_switch(hashCodes, new ProcessSwitchCallback() {
                public void processCase(int key, Label ignore_end) throws Exception {
                    int index = ((Integer)indexes.get(new Integer(key))).intValue();
                    String string = strings[index];
                    push(string);
                    invoke(MethodConstants.EQUALS);
                    ifeq(def);
                    callback.processCase(string, end);
                }
                public void processDefault() {
                    pop();
                }
            });
        mark(def);
        callback.processDefault();
        mark(end);
    }

    public void handle_undeclared(Class[] exceptionTypes, Block handler) {
        /* generates:
           } catch (RuntimeException e) {
               throw e;
           } catch (Error e) {
               throw e;
           } catch (<DeclaredException> e) {
               throw e;
           } catch (Throwable e) {
               throw new UndeclaredThrowableException(e);
           }
        */
        Set exceptionSet = new HashSet(Arrays.asList(exceptionTypes));
        if (!(exceptionSet.contains(Exception.class) ||
              exceptionSet.contains(Throwable.class))) {
            if (!exceptionSet.contains(RuntimeException.class)) {
                catch_exception(handler, RuntimeException.class);
                athrow();
            }
            if (!exceptionSet.contains(Error.class)) {
                catch_exception(handler, Error.class);
                athrow();
            }
            for (int i = 0; i < exceptionTypes.length; i++) {
                catch_exception(handler, exceptionTypes[i]);
                athrow();
            }
            // e -> eo -> oeo -> ooe -> o
            catch_exception(handler, Throwable.class);
            new_instance(UndeclaredThrowableException.class);
            dup_x1();
            swap();
            invoke_constructor(UndeclaredThrowableException.class, TYPES_THROWABLE);
            athrow();
        }
    }

    public void load_method(Method method) {
        load_class(method.getDeclaringClass());
        push(method.getName());
        push_object(method.getParameterTypes());
        invoke(MethodConstants.GET_DECLARED_METHOD);
    }
}
