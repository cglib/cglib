package net.sf.cglib.transform;

import net.sf.cglib.core.Constants;
import net.sf.cglib.core.Emitter2;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

public class ExampleTransformer extends EmittingTransformer {
    private final String[] newPropertyNames;
    private final Class[] newPropertyTypes;

    public ExampleTransformer(String[] newPropertyNames,
                              Class[] newPropertyTypes) {
        this.newPropertyNames = newPropertyNames;
        this.newPropertyTypes = newPropertyTypes;
    }

    private static String upfirst(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
    
    protected Emitter2 getEmitter(ClassVisitor cv) {
        return new Emitter2(cv) {
            public void end_class() {
                for (int i = 0; i < newPropertyNames.length; i++) {
                    String name = newPropertyNames[i];
                    Type type = Type.getType(newPropertyTypes[i]);
                    String fieldName = "HIB$" + name;
                    String methodName = upfirst(name);
                    declare_field(Constants.ACC_PRIVATE, fieldName, type, null);

                    begin_method(Constants.ACC_PUBLIC, "get" + methodName, type, new Type[0], null);
                    load_this();
                    getfield(fieldName);
                    return_value();

                    begin_method(Constants.ACC_PUBLIC, "set" + methodName, Type.VOID_TYPE, new Type[]{ type }, null);
                    load_this();
                    load_arg(0);
                    putfield(fieldName);
                    return_value();
                }
                super.end_class();
            }
        };
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ExampleTransformer{names=[");
        for (int i = 0; i < newPropertyNames.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(newPropertyNames[i]);
        }
        sb.append("]}");
        return sb.toString();
    }
}
