package net.sf.cglib.core;

import org.objectweb.asm.Type;

public class TypeUtils {
    private TypeUtils() {
    }

    public static Type getBoxedType(Type type) {
        switch (type.getSort()) {
        case Type.CHAR:
            return Constants.TYPE_CHARACTER;
        case Type.BOOLEAN:
            return Constants.TYPE_BOOLEAN;
        case Type.DOUBLE:
            return Constants.TYPE_DOUBLE;
        case Type.FLOAT:
            return Constants.TYPE_FLOAT;
        case Type.LONG:
            return Constants.TYPE_LONG;
        case Type.INT:
            return Constants.TYPE_INTEGER;
        case Type.SHORT:
            return Constants.TYPE_SHORT;
        case Type.BYTE:
            return Constants.TYPE_BYTE;
        default:
            return type;
        }
    }

    public static boolean isArray(Type type) {
        return type.getSort() == Type.ARRAY;
    }

    public static Type getComponentType(Type type) {
        if (!isArray(type)) {
            throw new IllegalArgumentException("Type " + type + " is not an array");
        }
        return Type.getType(type.getDescriptor().substring(1));
    }

    public static boolean isPrimitive(Type type) {
        switch (type.getSort()) {
        case Type.ARRAY:
        case Type.OBJECT:
            return false;
        default:
            return true;
        }
    }

    public static String emulateClassGetName(Type type) {
        if (isPrimitive(type) || isArray(type)) {
            return type.getDescriptor().replace('/', '.');
        } else {
            return type.getClassName();
        }
    }

    public static Type[] getTypes(Class[] classes) {
        if (classes == null) {
            return null;
        }
        Type[] types = new Type[classes.length];
        for (int i = 0; i < classes.length; i++) {
            types[i] = Type.getType(classes[i]);
        }
        return types;
    }
}
