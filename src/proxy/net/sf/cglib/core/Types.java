package net.sf.cglib.core;

import org.objectweb.asm.Type;

public interface Types {

    public static final Type[] EMPTY = {};

    public static final Type OBJECT_ARRAY = Signature.parseType("Object[]");
    public static final Type OBJECT = Signature.parseType("Object");
    public static final Type CLASS = Signature.parseType("Class");
    public static final Type BIG_INTEGER = Signature.parseType("java.math.BigInteger");
    public static final Type BIG_DECIMAL = Signature.parseType("java.math.BigDecimal");
    public static final Type CHARACTER = Signature.parseType("Character");
    public static final Type BOOLEAN = Signature.parseType("Boolean");
    public static final Type DOUBLE = Signature.parseType("Double");
    public static final Type FLOAT = Signature.parseType("Float");
    public static final Type LONG = Signature.parseType("Long");
    public static final Type INTEGER = Signature.parseType("Integer");
    public static final Type SHORT = Signature.parseType("Short");
    public static final Type BYTE = Signature.parseType("Byte");
    public static final Type NUMBER = Signature.parseType("Number");
    public static final Type STRING = Signature.parseType("String");
    public static final Type THREAD_LOCAL = Signature.parseType("ThreadLocal");
    public static final Type THROWABLE = Signature.parseType("Throwable");
    public static final Type ERROR = Signature.parseType("Error");
    public static final Type EXCEPTION = Signature.parseType("Exception");
    public static final Type RUNTIME_EXCEPTION = Signature.parseType("RuntimeException");
    public static final Type METHOD = Signature.parseType("java.lang.reflect.Method");
    public static final Type ILLEGAL_STATE_EXCEPTION = Signature.parseType("IllegalStateException");
    public static final Type ILLEGAL_ARGUMENT_EXCEPTION = Signature.parseType("IllegalArgumentException");
    public static final Type NO_CLASS_DEF_FOUND_ERROR = Signature.parseType("NoClassDefFoundError");
    public static final Type CLASS_NOT_FOUND_EXCEPTION = Signature.parseType("ClassNotFoundException");
    public static final Type ABSTRACT_METHOD_ERROR = Signature.parseType("AbstractMethodError");
    public static final Type CLASS_CAST_EXCEPTION = Signature.parseType("ClassCastException");
    public static final Type NO_SUCH_METHOD_ERROR = Signature.parseType("NoSuchMethodError");
}
    
