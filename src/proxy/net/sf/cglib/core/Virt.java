package net.sf.cglib.core;

import java.lang.reflect.*;
import java.util.*;
import java.math.*;
import net.sf.cglib.UndeclaredThrowableException;

public class Virt
{
    private Virt() { }

    public static final int SWITCH_STYLE_TRIE = 0;
    public static final int SWITCH_STYLE_HASH = 1;
    
    private static final Map primitiveMethods = new HashMap();

    private static final Class[] TYPES_STRING = { String.class };
    private static final Class[] TYPES_THROWABLE = { Throwable.class };

    private static final String FIND_CLASS = "CGLIB$findClass";

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

    public static void load_class_this(Emitter cg) {
        load_class_helper(cg, cg.getClassName());
    }
    
    public static void load_class(Emitter cg, Class type) {
        if (type.isPrimitive()) {
            if (type.equals(Void.TYPE)) {
                throw new IllegalArgumentException("cannot load void type");
            }
            try {
                cg.getfield(ReflectUtils.getBoxedType(type).getDeclaredField("TYPE"));
            } catch (NoSuchFieldException e) {
                throw new CodeGenerationException(e);
            }
        } else {
            load_class_helper(cg, type.getName());
        }
    }

    public static void load_method(Emitter cg, Method method) {
        load_class(cg, method.getDeclaringClass());
        cg.push(method.getName());
        push_object(cg, method.getParameterTypes());
        cg.invoke(MethodConstants.GET_DECLARED_METHOD);
    }

    private static void load_class_helper(final Emitter cg, String className) {
        cg.register(FIND_CLASS, new Emitter.FinalizeCallback() {
            public void process() {
                generateFindClass(cg);
            }
        });
        cg.push(className);
        cg.invoke_static_this(FIND_CLASS, Class.class, TYPES_STRING);
    }

    private static void generateFindClass(Emitter cg) {
        /* generates:
           static private Class findClass(String name) throws Exception {
               try {
                   return Class.forName(name);
               } catch (java.lang.ClassNotFoundException cne) {
                   throw new java.lang.NoClassDefFoundError(cne.getMessage());
               }
           }
         */
        cg.begin_method(Modifier.PRIVATE | Modifier.STATIC,
                        Class.class,
                        FIND_CLASS,
                        TYPES_STRING,
                        null);

        Block block = cg.begin_block();
        cg.load_arg(0);
        cg.invoke(MethodConstants.FOR_NAME);
        cg.return_value();
        cg.end_block();
        
        cg.catch_exception(block, ClassNotFoundException.class);
        cg.invoke(MethodConstants.THROWABLE_GET_MESSAGE);
        cg.new_instance(NoClassDefFoundError.class);
        cg.dup_x1();
        cg.swap();
        cg.invoke_constructor(NoClassDefFoundError.class, TYPES_STRING);
        cg.athrow();
        cg.end_method();
    }

    /**
     * Allocates and fills an Object[] array with the arguments to the
     * current method. Primitive values are inserted as their boxed
     * (Object) equivalents.
     */
    public static void create_arg_array(Emitter cg) {
        /* generates:
           Object[] args = new Object[]{ arg1, new Integer(arg2) };
         */
        Class[] parameterTypes = cg.getParameterTypes();
        cg.push(parameterTypes.length);
        cg.newarray();
        for (int i = 0; i < parameterTypes.length; i++) {
            cg.dup();
            cg.push(i);
            cg.load_arg(i);
            box(cg, parameterTypes[i]);
            cg.aastore();
        }
    }
    
    public static void push(Emitter cg, Object[] array) {
        cg.push(array.length);
        cg.newarray(array.getClass().getComponentType());
        for (int i = 0; i < array.length; i++) {
            cg.dup();
            cg.push(i);
            push_object(cg, array[i]);
            cg.aastore();
        }
    }
    
    public static void push_object(Emitter cg, Object obj) {
        if (obj == null) {
            cg.aconst_null();
        } else {
            Class type = obj.getClass();
            if (type.isArray()) {
                push(cg, (Object[])obj);
            } else if (obj instanceof String) {
                cg.push((String)obj);
            } else if (obj instanceof Class) {
                load_class(cg, (Class)obj);
            } else if (obj instanceof BigInteger) {
                cg.new_instance(BigInteger.class);
                cg.dup();
                cg.push(obj.toString());
                cg.invoke_constructor(BigInteger.class);
            } else if (obj instanceof BigDecimal) {
                cg.new_instance(BigDecimal.class);
                cg.dup();
                cg.push(obj.toString());
                cg.invoke_constructor(BigDecimal.class);
            } else if (obj instanceof Number) {
                push_unboxed(cg, obj);
            } else {
                throw new IllegalArgumentException("unknown type: " + obj.getClass());
            }
        }
    }

    public static void push(Emitter cg, boolean value) {
        cg.push(value ? 1 : 0);
    }

    /**
     * If the object is a Number, Boolean, or Character, pushes the equivalent primitive
     * value onto the stack. Otherwise, calls push_object(obj).
     */
    public static void push_unboxed(Emitter cg, Object obj)
    {
        if (obj == null || !ReflectUtils.getUnboxedType(obj.getClass()).isPrimitive()) {
            push_object(cg, obj);
        } else if (obj instanceof Boolean) {
            cg.push(((Boolean)obj).booleanValue() ? 1 : 0);
        } else if (obj instanceof Character) {
            cg.push((short)((Character)obj).charValue());
        } else if (obj instanceof Long) {
            cg.push(((Long)obj).longValue());
        } else if (obj instanceof Double) {
            cg.push(((Double)obj).doubleValue());
        } else if (obj instanceof Float) {
            cg.push(((Float)obj).floatValue());
        } else {
            cg.push(((Number)obj).intValue());
        }
    }
    
    /**
     * Pushes a zero onto the stack if the argument is a primitive class, or a null otherwise.
     */
    public static void zero_or_null(Emitter cg, Class type) {
        if (type.isPrimitive()) {
            if (type.equals(Double.TYPE)) {
                cg.push(0d);
            } else if (type.equals(Long.TYPE)) {
                cg.push(0L);
            } else if (type.equals(Float.TYPE)) {
                cg.push(0f);
            } else if (type.equals(Void.TYPE)) {
                // ignore
            } else {
                cg.push(0);
            }
        } else {
            cg.aconst_null();
        }
    }

    /**
     * Toggles the integer on the top of the stack from 1 to 0 or vice versa
     */
    public static void not(Emitter cg) {
        cg.push(1);
        cg.xor(Integer.TYPE);
    }
    
    /**
     * Unboxes the object on the top of the stack. If the object is null, the
     * unboxed primitive value becomes zero.
     */
    public static void unbox_or_zero(Emitter cg, Class type) {
        if (type.isPrimitive()) {
            if (!type.equals(Void.TYPE)) {
                Label nonNull = cg.make_label();
                Label end = cg.make_label();
                cg.dup();
                cg.ifnonnull(nonNull);
                cg.pop();
                zero_or_null(cg, type);
                cg.goTo(end);
                cg.mark(nonNull);
                unbox(cg, type);
                cg.mark(end);
            }
        } else {
            cg.checkcast(type);
        }
    }
    
    /**
     * If the argument is a primitive class, replaces the primitive value
     * on the top of the stack with the wrapped (Object) equivalent. For
     * example, char -> Character.
     * If the class is Void, a null is pushed onto the stack instead.
     * @param type the class indicating the current type of the top stack value
     */
    public static void box(Emitter cg, Class type) {
        if (type.isPrimitive()) {
            if (type.equals(Void.TYPE)) {
                cg.aconst_null();
            } else {
                Class wrapper = ReflectUtils.getBoxedType(type);
                cg.new_instance(wrapper);
                if (cg.getStackSize(type) == 2) {
                    // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
                    cg.dup_x2();
                    cg.dup_x2();
                    cg.pop();
                } else {
                    // p -> po -> opo -> oop -> o
                    cg.dup_x1();
                    cg.swap();
                }
                cg.invoke_constructor(wrapper, new Class[]{ type });
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
    public static void unbox(Emitter cg, Class type) {
        if (type.isPrimitive()) {
            if (!type.equals(Void.TYPE)) {
                Method convert = (Method)primitiveMethods.get(type);
                cg.checkcast(convert.getDeclaringClass());
                cg.invoke(convert);
            }
        } else {
            cg.checkcast(type);
        }
    }
    
    public static void null_constructor(Emitter cg) {
        cg.begin_constructor();
        cg.load_this();
        cg.super_invoke_constructor();
        cg.return_value();
        cg.end_method();
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
    public static void process_array(Emitter cg, Class type, ProcessArrayCallback callback) {
        Class compType = type.getComponentType();
        Local array = cg.make_local();
        Local loopvar = cg.make_local(Integer.TYPE);
        Label loopbody = cg.make_label();
        Label checkloop = cg.make_label();
        cg.store_local(array);
        cg.push(0);
        cg.store_local(loopvar);
        cg.goTo(checkloop);
        
        cg.mark(loopbody);
        cg.load_local(array);
        cg.load_local(loopvar);
        cg.array_load(compType);
        callback.processElement(compType);
        cg.iinc(loopvar, 1);
        
        cg.mark(checkloop);
        cg.load_local(loopvar);
        cg.load_local(array);
        cg.arraylength();
        cg.if_icmplt(loopbody);
    }
    
    /**
     * Process two arrays on the stack in parallel. Assumes the top two items on the stack
     * are arrays of the specified class. The arrays must be the same length. For each pair
     * of elements in the arrays, puts the pair on the stack and triggers the callback.
     * @param type the type of the arrays (type.isArray() must be true)
     * @param callback the callback triggered for each pair of elements
     */
    public static void process_arrays(Emitter cg, Class type, ProcessArrayCallback callback) {
        Class compType = type.getComponentType();
        Local array1 = cg.make_local();
        Local array2 = cg.make_local();
        Local loopvar = cg.make_local(Integer.TYPE);
        Label loopbody = cg.make_label();
        Label checkloop = cg.make_label();
        cg.store_local(array1);
        cg.store_local(array2);
        cg.push(0);
        cg.store_local(loopvar);
        cg.goTo(checkloop);
        
        cg.mark(loopbody);
        cg.load_local(array1);
        cg.load_local(loopvar);
        cg.array_load(compType);
        cg.load_local(array2);
        cg.load_local(loopvar);
        cg.array_load(compType);
        callback.processElement(compType);
        cg.iinc(loopvar, 1);
        
        cg.mark(checkloop);
        cg.load_local(loopvar);
        cg.load_local(array1);
        cg.arraylength();
        cg.if_icmplt(loopbody);
    }
    
    /**
     * Branches to the specified label if the top two items on the stack
     * are not equal. The items must both be of the specified
     * class. Equality is determined by comparing primitive values
     * directly and by invoking the <code>equals</code> method for
     * Objects. Arrays are recursively processed in the same manner.
     */
    public static void not_equals(final Emitter cg, Class type, final Label notEquals) {
        (new ProcessArrayCallback() {
            public void processElement(Class type) {
                not_equals_helper(cg, type, notEquals, this);
            }
        }).processElement(type);
    }
    
    private static void not_equals_helper(Emitter cg, Class type, Label notEquals, ProcessArrayCallback callback) {
        if (type.isPrimitive()) {
            cg.if_cmpne(type, notEquals);
        } else {
            Label end = cg.make_label();
            nullcmp(cg, notEquals, end);
            if (type.isArray()) {
                Label checkContents = cg.make_label();
                cg.dup2();
                cg.arraylength();
                cg.swap();
                cg.arraylength();
                cg.if_icmpeq(checkContents);
                cg.pop2();
                cg.goTo(notEquals);
                cg.mark(checkContents);
                process_arrays(cg, type, callback);
            } else {
                cg.invoke(MethodConstants.EQUALS);
                cg.ifeq(notEquals);
            }
            cg.mark(end);
        }
    }
    
    public static void throw_exception(Emitter cg, Class type, String msg) {
        cg.new_instance(type);
        cg.dup();
        cg.push(msg);
        cg.invoke_constructor(type, new Class[]{ String.class });
        cg.athrow();
    }
    
    ///// TODO: get rid of this
    /**
     * If both objects on the top of the stack are non-null, does nothing.
     * If one is null, or both are null, both are popped off and execution
     * branches to the respective label.
     * @param oneNull label to branch to if only one of the objects is null
     * @param bothNull label to branch to if both of the objects are null
     */
    public static void nullcmp(Emitter cg, Label oneNull, Label bothNull) {
        cg.dup2();
        Label nonNull = cg.make_label();
        Label oneNullHelper = cg.make_label();
        Label end = cg.make_label();
        cg.ifnonnull(nonNull);
        cg.ifnonnull(oneNullHelper);
        cg.pop2();
        cg.goTo(bothNull);
        
        cg.mark(nonNull);
        cg.ifnull(oneNullHelper);
        cg.goTo(end);
        
        cg.mark(oneNullHelper);
        cg.pop2();
        cg.goTo(oneNull);
        
        cg.mark(end);
    }
    
    public static void factory_method(Emitter cg, Method method) {
        cg.begin_method(method);
        cg.new_instance_this();
        cg.dup();
        cg.load_args();
        cg.invoke_constructor_this(method.getParameterTypes());
        cg.return_value();
        cg.end_method();
    }

    public interface ObjectSwitchCallback {
        void processCase(Object key, Label end) throws Exception;
        void processDefault() throws Exception;
    }

    public static void string_switch(Emitter cg, String[] strings, int switchStyle, ObjectSwitchCallback callback)
    throws Exception {
        switch (switchStyle) {
        case SWITCH_STYLE_TRIE:
            string_switch_trie(cg, strings, callback);
            break;
        case SWITCH_STYLE_HASH:
            string_switch_hash(cg, strings, callback);
            break;
        default:
            throw new IllegalArgumentException("unknown switch style " + switchStyle);
        }
    }

    private static void string_switch_trie(final Emitter cg,
                                           String[] strings,
                                           final ObjectSwitchCallback callback) throws Exception {
        final Label def = cg.make_label();
        final Label end = cg.make_label();
        final Map buckets = CollectionUtils.bucket(Arrays.asList(strings), new Transformer() {
            public Object transform(Object value) {
                return new Integer(((String)value).length());
            }
        });
        cg.dup();
        cg.invoke(MethodConstants.STRING_LENGTH);
        cg.process_switch(getSwitchKeys(buckets), new Emitter.ProcessSwitchCallback() {
                public void processCase(int key, Label ignore_end) throws Exception {
                    List bucket = (List)buckets.get(new Integer(key));
                    stringSwitchHelper(cg, bucket, callback, def, end, 0);
                }
                public void processDefault() {
                    cg.goTo(def);
                }
            });
        cg.mark(def);
        cg.pop();
        callback.processDefault();
        cg.mark(end);
    }

    private static void stringSwitchHelper(final Emitter cg,
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
        cg.dup();
        cg.push(index);
        cg.invoke(MethodConstants.STRING_CHAR_AT);
        cg.process_switch(getSwitchKeys(buckets), new Emitter.ProcessSwitchCallback() {
                public void processCase(int key, Label ignore_end) throws Exception {
                    List bucket = (List)buckets.get(new Integer(key));
                    if (index + 1 == len) {
                        cg.pop();
                        callback.processCase(bucket.get(0), end);
                    } else {
                        stringSwitchHelper(cg, bucket, callback, def, end, index + 1);
                    }
                }
                public void processDefault() {
                    cg.goTo(def);
                }
            });
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

    private static void string_switch_hash(final Emitter cg,
                                           final String[] strings,
                                           final ObjectSwitchCallback callback) throws Exception {
        final Map buckets = CollectionUtils.bucket(Arrays.asList(strings), new Transformer() {
            public Object transform(Object value) {
                return new Integer(value.hashCode());
            }
        });
        final Label def = cg.make_label();
        final Label end = cg.make_label();
        cg.dup();
        cg.invoke(MethodConstants.HASH_CODE);
        cg.process_switch(getSwitchKeys(buckets), new Emitter.ProcessSwitchCallback() {
            public void processCase(int key, Label ignore_end) throws Exception {
                List bucket = (List)buckets.get(new Integer(key));
                Label next = null;
                for (Iterator it = bucket.iterator(); it.hasNext();) {
                    String string = (String)it.next();
                    if (next != null) {
                        cg.mark(next);
                    }
                    if (it.hasNext()) {
                        cg.dup();
                    }
                    cg.push(string);
                    cg.invoke(MethodConstants.EQUALS);
                    if (it.hasNext()) {
                        cg.ifeq(next = cg.make_label());
                        cg.pop();
                    } else {
                        cg.ifeq(def);
                    }
                    callback.processCase(string, end);
                }
            }
            public void processDefault() {
                cg.pop();
            }
        });
        cg.mark(def);
        callback.processDefault();
        cg.mark(end);
    }

    public static void handle_undeclared(Emitter cg, Class[] exceptionTypes, Block handler) {
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
                cg.catch_exception(handler, RuntimeException.class);
                cg.athrow();
            }
            if (!exceptionSet.contains(Error.class)) {
                cg.catch_exception(handler, Error.class);
                cg.athrow();
            }
            for (int i = 0; i < exceptionTypes.length; i++) {
                cg.catch_exception(handler, exceptionTypes[i]);
                cg.athrow();
            }
            // e -> eo -> oeo -> ooe -> o
            cg.catch_exception(handler, Throwable.class);
            cg.new_instance(UndeclaredThrowableException.class);
            cg.dup_x1();
            cg.swap();
            cg.invoke_constructor(UndeclaredThrowableException.class, TYPES_THROWABLE);
            cg.athrow();
        }
    }

    private interface ParameterTyper {
        Class[] getParameterTypes(Object member);
    }

    public static void method_switch(Emitter cg,
                                     Method[] methods,
                                     ObjectSwitchCallback callback) throws Exception {
        member_switch_helper(cg, Arrays.asList(methods), callback, true, new ParameterTyper() {
            public Class[] getParameterTypes(Object member) {
                return ((Method)member).getParameterTypes();
            }
        });
    }

    public static void constructor_switch(Emitter cg,
                                          Constructor[] cstructs,
                                          ObjectSwitchCallback callback) throws Exception {
        member_switch_helper(cg, Arrays.asList(cstructs), callback, false, new ParameterTyper() {
            public Class[] getParameterTypes(Object member) {
                return ((Constructor)member).getParameterTypes();
            }
        });
    }

    private static void member_switch_helper(final Emitter cg,
                                             List members,
                                             final ObjectSwitchCallback callback,
                                             boolean useName,
                                             final ParameterTyper typer) throws Exception {
        final Map cache = new HashMap();
        final ParameterTyper cached = new ParameterTyper() {
            public Class[] getParameterTypes(Object member) {
                Class[] types = (Class[])cache.get(member);
                if (types == null) {
                    cache.put(member, types = typer.getParameterTypes(member));
                }
                return types;
            }
        };
        final Label def = cg.make_label();
        final Label end = cg.make_label();
        if (useName) {
            cg.swap();
            final Map buckets = CollectionUtils.bucket(members, new Transformer() {
                public Object transform(Object value) {
                    return ((Member)value).getName();
                }
            });
            String[] names = (String[])buckets.keySet().toArray(new String[buckets.size()]);
            string_switch_hash(cg, names, new ObjectSwitchCallback() {
                public void processCase(Object key, Label dontUseEnd) throws Exception {
                    member_helper_size(cg, (List)buckets.get(key), callback, cached, def, end);
                }
                public void processDefault() throws Exception {
                    cg.goTo(def);
                }
            });
        } else {
            member_helper_size(cg, members, callback, cached, def, end);
        }
        cg.mark(def);
        cg.pop();
        callback.processDefault();
        cg.mark(end);
    }

    private static void member_helper_size(final Emitter cg,
                                           List members,
                                           final ObjectSwitchCallback callback,
                                           final ParameterTyper typer,
                                           final Label def,
                                           final Label end) throws Exception {
        final Map buckets = CollectionUtils.bucket(members, new Transformer() {
            public Object transform(Object value) {
                return new Integer(typer.getParameterTypes(value).length);
            }
        });
        cg.dup();
        cg.arraylength();
        cg.process_switch(getSwitchKeys(buckets), new Emitter.ProcessSwitchCallback() {
            public void processCase(int key, Label dontUseEnd) throws Exception {
                List bucket = (List)buckets.get(new Integer(key));
                Class[] types = typer.getParameterTypes(bucket.get(0));
                member_helper_type(cg, bucket, callback, typer, def, end, new BitSet(types.length));
            }
            public void processDefault() throws Exception {
                cg.goTo(def);
            }
        });
    }

    private static void member_helper_type(final Emitter cg,
                                           List members,
                                           final ObjectSwitchCallback callback,
                                           final ParameterTyper typer,
                                           final Label def,
                                           final Label end,
                                           final BitSet checked) throws Exception {
        if (members.size() == 1) {
            // need to check classes that have not already been checked via switches
            Member member = (Member)members.get(0);
            Class[] types = typer.getParameterTypes(member);
            for (int i = 0; i < types.length; i++) {
                if (checked == null || !checked.get(i)) {
                    cg.dup();
                    cg.aaload(i);
                    cg.invoke(MethodConstants.CLASS_GET_NAME);
                    cg.push(types[i].getName());
                    cg.invoke(MethodConstants.EQUALS);
                    cg.ifeq(def);
                }
            }
            cg.pop();
            callback.processCase(member, end);
        } else {
            // choose the index that has the best chance of uniquely identifying member
            Class[] example = typer.getParameterTypes(members.get(0));
            Map buckets = null;
            int index = -1;
            for (int i = 0; i < example.length; i++) {
                final int j = i;
                Map test = CollectionUtils.bucket(members, new Transformer() {
                    public Object transform(Object value) {
                        return typer.getParameterTypes(value)[j].getName();
                    }
                });
                if (buckets == null || test.size() > buckets.size()) {
                    buckets = test;
                    index = i;
                }
            }
            if (buckets == null) {
                // must have two methods with same name, types, and different return types
                cg.goTo(def);
            } else {
                checked.set(index);

                cg.dup();
                cg.aaload(index);
                cg.invoke(MethodConstants.CLASS_GET_NAME);

                final Map fbuckets = buckets;
                String[] names = (String[])buckets.keySet().toArray(new String[buckets.size()]);
                string_switch_hash(cg, names, new ObjectSwitchCallback() {
                    public void processCase(Object key, Label dontUseEnd) throws Exception {
                        member_helper_type(cg, (List)fbuckets.get(key), callback, typer, def, end, checked);
                    }
                    public void processDefault() throws Exception {
                        cg.goTo(def);
                    }
                });
            }
        }
    }
}
