package net.sf.cglib.core;

import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.objectweb.asm.Type;

public class Signature {
    private static final Map transforms = new HashMap(8);
    private String name;
    private String desc;

    public Signature(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public Signature(Method method) {
        this(method.getName(), Type.getMethodDescriptor(method));
    }

    public Signature(Constructor constructor) {
        this(Constants.CONSTRUCTOR_NAME,
             Type.getMethodDescriptor(Type.VOID_TYPE, getTypes(constructor.getParameterTypes())));
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

    public static Signature parse(String s) {
        int space = s.indexOf(' ');
        int lparen = s.indexOf('(', space);
        int rparen = s.indexOf(')', lparen);
        String returnType = s.substring(0, space);
        String methodName = s.substring(space + 1, lparen);
        StringBuffer sb = new StringBuffer();
        int mark = lparen;
        sb.append('(');
        for (;;) {
            int next = s.indexOf(',', mark + 1);
            if (next < 0) {
                sb.append(map(s.substring(mark + 1, rparen).trim()));
                break;
            } else {
                sb.append(map(s.substring(mark + 1, next).trim()));
                mark = next;
            }
        }
        sb.append(')');
        sb.append(map(returnType));
        return new Signature(methodName, sb.toString());
    }

    public static Type parseType(String s) {
        return Type.getType(map(s));
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

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return desc;
    }

    public Type getReturnType() {
        return Type.getReturnType(desc);
    }

    public Type[] getArgumentTypes() {
        return Type.getArgumentTypes(desc);
    }

    public String toString() {
        return name + desc;
    }
}
