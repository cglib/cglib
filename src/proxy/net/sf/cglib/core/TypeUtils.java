package net.sf.cglib.core;

import org.objectweb.asm.Type;
import java.util.*;

public class TypeUtils {
    private static final Map transforms = new HashMap(8);

    private TypeUtils() {
    }

    static {
        transforms.put("void", "V");
        transforms.put("byte", "B");
        transforms.put("char", "C");
        transforms.put("double", "D");
        transforms.put("float", "F");
        transforms.put("int", "I");
        transforms.put("long", "J");
        transforms.put("short", "S");
        transforms.put("boolean", "Z");
    }


    public static Signature parseSignature(String s) {
        int space = s.indexOf(' ');
        int lparen = s.indexOf('(', space);
        int rparen = s.indexOf(')', lparen);
        String returnType = s.substring(0, space);
        String methodName = s.substring(space + 1, lparen);
        StringBuffer sb = new StringBuffer();
        int mark = lparen + 1;
        sb.append('(');
        for (Iterator it = parseTypes(s, lparen + 1, rparen).iterator(); it.hasNext();) {
            sb.append(it.next());
        }
        sb.append(')');
        sb.append(map(returnType));
        return new Signature(methodName, sb.toString());
    }

    public static Type parseType(String s) {
        return Type.getType(map(s));
    }

    public static Type[] parseTypes(String s) {
        List names = parseTypes(s, 0, s.length());
        Type[] types = new Type[names.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = Type.getType((String)names.get(i));
        }
        return types;
    }

    public static Signature parseConstructor(Type[] types) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        for (int i = 0; i < types.length; i++) {
            sb.append(types[i].getDescriptor());
        }
        sb.append(")");
        sb.append("V");
        return new Signature(Constants.CONSTRUCTOR_NAME, sb.toString());
    }

    public static Signature parseConstructor(String sig) {
        return parseSignature("void <init>(" + sig + ")"); // TODO
    }

    private static List parseTypes(String s, int mark, int end) {
        List types = new ArrayList(5);
        for (;;) {
            int next = s.indexOf(',', mark);
            if (next < 0) {
                break;
            }
            types.add(map(s.substring(mark, next).trim()));
            mark = next + 1;
        }
        types.add(map(s.substring(mark, end).trim()));
        return types;
    }

    private static String map(String type) {
        if (type.equals("")) {
            return type;
        }
        String t = (String)transforms.get(type);
        if (t != null) {
            return t;
        } else if (type.indexOf('.') < 0) {
            return map("java.lang." + type);
        } else {
            StringBuffer sb = new StringBuffer();
            int index = 0;
            while ((index = type.indexOf("[]", index) + 1) > 0) {
                sb.append('[');
            }
            type = type.substring(0, type.length() - sb.length() * 2);
            sb.append('L').append(type.replace('.', '/')).append(';');
            return sb.toString();
        }
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
