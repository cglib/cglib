package net.sf.cglib.core;

import org.objectweb.asm.Type;

public interface Signatures {
    public static final Signature EQUALS =
      Signature.parse("boolean equals(Object)");
    public static final Signature GET_DECLARED_METHOD =
      Signature.parse("java.lang.reflect.Method getDeclaredMethod(String, Class[])");
    public static final Signature HASH_CODE =
      Signature.parse("int hashCode()");
    public static final Signature STRING_LENGTH =
      Signature.parse("int length()");
    public static final Signature STRING_CHAR_AT =
      Signature.parse("char charAt(int)");
    public static final Signature FOR_NAME =
      Signature.parse("Class forName(String)");

    public static final Signature GET_NAME =
      Signature.parse("String getName()");
    public static final Signature GET_MESSAGE =
      Signature.parse("String getMessage()");

    public static final Signature BOOLEAN_VALUE =
      Signature.parse("boolean booleanValue()");
    public static final Signature CHAR_VALUE =
      Signature.parse("char charValue()");
    public static final Signature LONG_VALUE =
      Signature.parse("long longValue()");
    public static final Signature DOUBLE_VALUE =
      Signature.parse("double doubleValue()");
    public static final Signature FLOAT_VALUE =
      Signature.parse("float floatValue()");
    public static final Signature INT_VALUE =
      Signature.parse("int intValue()");

    public static final Signature GET =
      Signature.parse("Object get(Object)");
    public static final Signature PUT =
      Signature.parse("Object put(Object, Object)");
    public static final Signature KEY_SET =
      Signature.parse("java.util.Set keySet()");

    public static final Signature STATIC =
      Signature.parse("void <clinit>()");

    public static final Signature CSTRUCT_OBJECT =
      Signature.parse("void <init>(Object)");
    public static final Signature CSTRUCT_CLASS =
      Signature.parse("void <init>(Class)");
    public static final Signature CSTRUCT_OBJECT_ARRAY =
      Signature.parse("void <init>(Object[])");
    public static final Signature CSTRUCT_STRING =
      Signature.parse("void <init>(String)");
    public static final Signature CSTRUCT_STRING_ARRAY =
      Signature.parse("void <init>(String[])");
    public static final Signature CSTRUCT_THROWABLE =
      Signature.parse("void <init>(Throwable)");
}
