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

import java.util.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public class Ops {
    public static final Signature HASH_CODE =
      Signature.parse("int hashCode()");
    public static final Signature EQUALS =
      Signature.parse("boolean equals(Object)");
    public static final Signature STRING_LENGTH =
      Signature.parse("int length()");
    public static final Signature STRING_CHAR_AT =
      Signature.parse("char charAt(int)");
    public static final Signature FOR_NAME =
      Signature.parse("Class forName(String)");
    public static final Signature GET_MESSAGE =
      Signature.parse("String getMessage()");
    private static final Signature BOOLEAN_VALUE =
      Signature.parse("boolean booleanValue()");
    private static final Signature CHAR_VALUE =
      Signature.parse("char charValue()");
    private static final Signature LONG_VALUE =
      Signature.parse("long longValue()");
    private static final Signature DOUBLE_VALUE =
      Signature.parse("double doubleValue()");
    private static final Signature FLOAT_VALUE =
      Signature.parse("float floatValue()");
    private static final Signature INT_VALUE =
      Signature.parse("int intValue()");
    private static final Signature CSTRUCT_STRING =
      Signature.parse("void <init>(String)");
    
    public static final int SWITCH_STYLE_TRIE = 0;
    public static final int SWITCH_STYLE_HASH = 1;
    private static final Signature FIND_CLASS =
      Signature.parse("Class CGLIB$findClass(String)");

    private Ops() {
    }
    
    /**
     * Allocates and fills an Object[] array with the arguments to the
     * current method. Primitive values are inserted as their boxed
     * (Object) equivalents.
     */
    public static void create_arg_array(Emitter e) {
        /* generates:
           Object[] args = new Object[]{ arg1, new Integer(arg2) };
         */
        Type[] argumentTypes = e.getArgumentTypes();
        e.push(argumentTypes.length);
        e.newarray();
        for (int i = 0; i < argumentTypes.length; i++) {
            e.dup();
            e.push(i);
            e.load_arg(i);
            box(e, argumentTypes[i]);
            e.aastore();
        }
    }
    
    /**
     * Pushes a zero onto the stack if the argument is a primitive class, or a null otherwise.
     */
    public static void zero_or_null(Emitter e, Type type) {
        if (TypeUtils.isPrimitive(type)) {
            switch (type.getSort()) {
            case Type.DOUBLE:
                e.push(0d);
                break;
            case Type.LONG:
                e.push(0L);
                break;
            case Type.FLOAT:
                e.push(0f);
                break;
            case Type.VOID:
                e.aconst_null();
            default:
                e.push(0);
            }
        } else {
            e.aconst_null();
        }
    }

    /**
     * Unboxes the object on the top of the stack. If the object is null, the
     * unboxed primitive value becomes zero.
     */
    public static void unbox_or_zero(Emitter e, Type type) {
        if (TypeUtils.isPrimitive(type)) {
            if (type != Type.VOID_TYPE) {
                Label nonNull = e.make_label();
                Label end = e.make_label();
                e.dup();
                e.ifnonnull(nonNull);
                e.pop();
                zero_or_null(e, type);
                e.goTo(end);
                e.mark(nonNull);
                unbox(e, type);
                e.mark(end);
            }
        } else {
            e.checkcast(type);
        }
    }

     /**
      * If the argument is a primitive class, replaces the primitive value
      * on the top of the stack with the wrapped (Object) equivalent. For
      * example, char -> Character.
      * If the class is Void, a null is pushed onto the stack instead.
      * @param type the class indicating the current type of the top stack value
      */
     public static void box(Emitter e, Type type) {
         if (TypeUtils.isPrimitive(type)) {
             if (type == Type.VOID_TYPE) {
                 e.aconst_null();
             } else {
                 Type boxed = TypeUtils.getBoxedType(type);
                 e.new_instance(boxed);
                 if (type.getSize() == 2) {
                     // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
                     e.dup_x2();
                     e.dup_x2();
                     e.pop();
                 } else {
                     // p -> po -> opo -> oop -> o
                     e.dup_x1();
                     e.swap();
                 }
                 e.invoke_constructor(boxed, new Type[]{ type });
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
    public static void unbox(Emitter e, Type type) {
        Type t = Constants.TYPE_NUMBER;
        Signature sig = null;
        switch (type.getSort()) {
        case Type.VOID:
            return;
        case Type.CHAR:
            t = Constants.TYPE_CHARACTER;
            sig = CHAR_VALUE;
            break;
        case Type.BOOLEAN:
            t = Constants.TYPE_BOOLEAN;
            sig = BOOLEAN_VALUE;
            break;
        case Type.DOUBLE:
            sig = DOUBLE_VALUE;
            break;
        case Type.FLOAT:
            sig = FLOAT_VALUE;
            break;
        case Type.LONG:
            sig = LONG_VALUE;
            break;
        case Type.INT:
        case Type.SHORT:
        case Type.BYTE:
            sig = INT_VALUE;
        }

        if (sig == null) {
            e.checkcast(type);
        } else {
            e.checkcast(t);
            e.invoke_virtual(t, sig);
        }
    }

    /**
     * Process an array on the stack. Assumes the top item on the stack
     * is an array of the specified type. For each element in the array,
     * puts the element on the stack and triggers the callback.
     * @param type the type of the array (type.isArray() must be true)
     * @param callback the callback triggered for each element
     */
    public static void process_array(Emitter e, Type type, ProcessArrayCallback callback) {
        Type componentType = TypeUtils.getComponentType(type);
        Local array = e.make_local();
        Local loopvar = e.make_local(Type.INT_TYPE);
        Label loopbody = e.make_label();
        Label checkloop = e.make_label();
        e.store_local(array);
        e.push(0);
        e.store_local(loopvar);
        e.goTo(checkloop);
        
        e.mark(loopbody);
        e.load_local(array);
        e.load_local(loopvar);
        e.array_load(componentType);
        callback.processElement(componentType);
        e.iinc(loopvar, 1);
        
        e.mark(checkloop);
        e.load_local(loopvar);
        e.load_local(array);
        e.arraylength();
        e.if_icmplt(loopbody);
    }
    
    /**
     * Process two arrays on the stack in parallel. Assumes the top two items on the stack
     * are arrays of the specified class. The arrays must be the same length. For each pair
     * of elements in the arrays, puts the pair on the stack and triggers the callback.
     * @param type the type of the arrays (type.isArray() must be true)
     * @param callback the callback triggered for each pair of elements
     */
    public static void process_arrays(Emitter e, Type type, ProcessArrayCallback callback) {
        Type componentType = TypeUtils.getComponentType(type);
        Local array1 = e.make_local();
        Local array2 = e.make_local();
        Local loopvar = e.make_local(Type.INT_TYPE);
        Label loopbody = e.make_label();
        Label checkloop = e.make_label();
        e.store_local(array1);
        e.store_local(array2);
        e.push(0);
        e.store_local(loopvar);
        e.goTo(checkloop);
        
        e.mark(loopbody);
        e.load_local(array1);
        e.load_local(loopvar);
        e.array_load(componentType);
        e.load_local(array2);
        e.load_local(loopvar);
        e.array_load(componentType);
        callback.processElement(componentType);
        e.iinc(loopvar, 1);
        
        e.mark(checkloop);
        e.load_local(loopvar);
        e.load_local(array1);
        e.arraylength();
        e.if_icmplt(loopbody);
    }
    
    /**
     * Branches to the specified label if the top two items on the stack
     * are not equal. The items must both be of the specified
     * class. Equality is determined by comparing primitive values
     * directly and by invoking the <code>equals</code> method for
     * Objects. Arrays are recursively processed in the same manner.
     */
    public static void not_equals(final Emitter e, Type type, final Label notEquals) {
        (new ProcessArrayCallback() {
            public void processElement(Type type) {
                not_equals_helper(e, type, notEquals, this);
            }
        }).processElement(type);
    }
    
    private static void not_equals_helper(Emitter e, Type type, Label notEquals, ProcessArrayCallback callback) {
        if (TypeUtils.isPrimitive(type)) {
            e.if_cmpne(type, notEquals);
        } else {
            Label end = e.make_label();
            nullcmp(e, notEquals, end);
            if (TypeUtils.isArray(type)) {
                Label checkContents = e.make_label();
                e.dup2();
                e.arraylength();
                e.swap();
                e.arraylength();
                e.if_icmpeq(checkContents);
                e.pop2();
                e.goTo(notEquals);
                e.mark(checkContents);
                process_arrays(e, type, callback);
            } else {
                e.invoke_virtual(Constants.TYPE_OBJECT, EQUALS);
                e.ifeq(notEquals);
            }
            e.mark(end);
        }
    }
    
    ///// TODO: get rid of this
    /**
     * If both objects on the top of the stack are non-null, does nothing.
     * If one is null, or both are null, both are popped off and execution
     * branches to the respective label.
     * @param oneNull label to branch to if only one of the objects is null
     * @param bothNull label to branch to if both of the objects are null
     */
    private static void nullcmp(Emitter e, Label oneNull, Label bothNull) {
        e.dup2();
        Label nonNull = e.make_label();
        Label oneNullHelper = e.make_label();
        Label end = e.make_label();
        e.ifnonnull(nonNull);
        e.ifnonnull(oneNullHelper);
        e.pop2();
        e.goTo(bothNull);
        
        e.mark(nonNull);
        e.ifnull(oneNullHelper);
        e.goTo(end);
        
        e.mark(oneNullHelper);
        e.pop2();
        e.goTo(oneNull);
        
        e.mark(end);
    }
    
    public static void string_switch(Emitter e, String[] strings, int switchStyle, ObjectSwitchCallback callback)
    throws Exception {
        switch (switchStyle) {
        case SWITCH_STYLE_TRIE:
            string_switch_trie(e, strings, callback);
            break;
        case SWITCH_STYLE_HASH:
            string_switch_hash(e, strings, callback);
            break;
        default:
            throw new IllegalArgumentException("unknown switch style " + switchStyle);
        }
    }

    static void string_switch_trie(final Emitter e,
                                   String[] strings,
                                   final ObjectSwitchCallback callback) throws Exception {
        final Label def = e.make_label();
        final Label end = e.make_label();
        final Map buckets = CollectionUtils.bucket(Arrays.asList(strings), new Transformer() {
            public Object transform(Object value) {
                return new Integer(((String)value).length());
            }
        });
        e.dup();
        e.invoke_virtual(Constants.TYPE_STRING, STRING_LENGTH);
        e.process_switch(getSwitchKeys(buckets), new ProcessSwitchCallback() {
                public void processCase(int key, Label ignore_end) throws Exception {
                    List bucket = (List)buckets.get(new Integer(key));
                    stringSwitchHelper(e, bucket, callback, def, end, 0);
                }
                public void processDefault() {
                    e.goTo(def);
                }
            });
        e.mark(def);
        e.pop();
        callback.processDefault();
        e.mark(end);
    }

    private static void stringSwitchHelper(final Emitter e,
                                           List strings,
                                           final ObjectSwitchCallback callback,
                                           final Label def,
                                           final Label end,
                                           final int index) throws Exception {
        final int len = ((String)strings.get(0)).length();
        final Map buckets = CollectionUtils.bucket(strings, new Transformer() {
            public Object transform(Object value) {
                return new Integer(((String)value).charAt(index));
            }
        });
        e.dup();
        e.push(index);
        e.invoke_virtual(Constants.TYPE_STRING, STRING_CHAR_AT);
        e.process_switch(getSwitchKeys(buckets), new ProcessSwitchCallback() {
                public void processCase(int key, Label ignore_end) throws Exception {
                    List bucket = (List)buckets.get(new Integer(key));
                    if (index + 1 == len) {
                        e.pop();
                        callback.processCase(bucket.get(0), end);
                    } else {
                        stringSwitchHelper(e, bucket, callback, def, end, index + 1);
                    }
                }
                public void processDefault() {
                    e.goTo(def);
                }
            });
    }        

    static int[] getSwitchKeys(Map buckets) {
        int[] keys = new int[buckets.size()];
        int index = 0;
        for (Iterator it = buckets.keySet().iterator(); it.hasNext();) {
            keys[index++] = ((Integer)it.next()).intValue();
        }
        Arrays.sort(keys);
        return keys;
    }

    static void string_switch_hash(final Emitter e,
                                   final String[] strings,
                                   final ObjectSwitchCallback callback) throws Exception {
        final Map buckets = CollectionUtils.bucket(Arrays.asList(strings), new Transformer() {
            public Object transform(Object value) {
                return new Integer(value.hashCode());
            }
        });
        final Label def = e.make_label();
        final Label end = e.make_label();
        e.dup();
        e.invoke_virtual(Constants.TYPE_OBJECT, HASH_CODE);
        e.process_switch(getSwitchKeys(buckets), new ProcessSwitchCallback() {
            public void processCase(int key, Label ignore_end) throws Exception {
                List bucket = (List)buckets.get(new Integer(key));
                Label next = null;
                for (Iterator it = bucket.iterator(); it.hasNext();) {
                    String string = (String)it.next();
                    if (next != null) {
                        e.mark(next);
                    }
                    if (it.hasNext()) {
                        e.dup();
                    }
                    e.push(string);
                    e.invoke_virtual(Constants.TYPE_OBJECT, EQUALS);
                    if (it.hasNext()) {
                        e.ifeq(next = e.make_label());
                        e.pop();
                    } else {
                        e.ifeq(def);
                    }
                    callback.processCase(string, end);
                }
            }
            public void processDefault() {
                e.pop();
            }
        });
        e.mark(def);
        callback.processDefault();
        e.mark(end);
    }

    public static void load_class_this(Emitter e) {
        load_class_helper(e, e.getClassType());
    }
    
    public static void load_class(Emitter e, Type type) {
        if (TypeUtils.isPrimitive(type)) {
            if (type == Type.VOID_TYPE) {
                throw new IllegalArgumentException("cannot load void type");
            }
            e.getstatic(TypeUtils.getBoxedType(type), "TYPE", Constants.TYPE_CLASS);
        } else {
            load_class_helper(e, type);
        }
    }

    private static void load_class_helper(final Emitter e, Type type) {
        e.register(FIND_CLASS, new Emitter.FinalizeCallback() {
            public void process() {
                generateFindClass(e);
            }
        });
        e.push(TypeUtils.emulateClassGetName(type));
        e.invoke_static_this(FIND_CLASS);
    }

    private static void generateFindClass(Emitter e) {
        /* generates:
           static private Class findClass(String name) throws Exception {
               try {
                   return Class.forName(name);
               } catch (java.lang.ClassNotFoundException cne) {
                   throw new java.lang.NoClassDefFoundError(cne.getMessage());
               }
           }
         */
        e.begin_method(Constants.PRIVATE_FINAL_STATIC, FIND_CLASS, null);
        Block block = e.begin_block();
        e.load_arg(0);
        e.invoke_static(Constants.TYPE_CLASS, FOR_NAME);
        e.return_value();
        e.end_block();
        e.catch_exception(block, Constants.TYPE_CLASS_NOT_FOUND_EXCEPTION);
        e.invoke_virtual(Constants.TYPE_THROWABLE, GET_MESSAGE);
        e.new_instance(Constants.TYPE_NO_CLASS_DEF_FOUND_ERROR);
        e.dup_x1();
        e.swap();
        e.invoke_constructor(Constants.TYPE_NO_CLASS_DEF_FOUND_ERROR, CSTRUCT_STRING);
        e.athrow();
    }
}
