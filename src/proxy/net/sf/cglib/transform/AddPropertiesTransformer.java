package net.sf.cglib.transform;

import net.sf.cglib.core.*;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

public class AddPropertiesTransformer extends EmittingTransformer {
    private final String[] newPropertyNames;
    private final Class[] newPropertyTypes;

    public AddPropertiesTransformer(String[] newPropertyNames,
                              Class[] newPropertyTypes) {
        this.newPropertyNames = newPropertyNames;
        this.newPropertyTypes = newPropertyTypes;
    }

    protected Emitter getEmitter(ClassVisitor cv) {
        return new Emitter(cv) {
            public void end_class() {
                for (int i = 0; i < newPropertyNames.length; i++) {
                    String name = newPropertyNames[i];
                    Type type = Type.getType(newPropertyTypes[i]);
                    String fieldName = "HIB$" + name;
                    declare_field(Constants.ACC_PRIVATE, fieldName, type, null);

                    String methodName = TypeUtils.upperFirst(name);
                    begin_method(Constants.ACC_PUBLIC, new Signature("get" + methodName, type, new Type[0]), null);
                    load_this();
                    getfield(fieldName);
                    return_value();

                    begin_method(Constants.ACC_PUBLIC, new Signature("set" + methodName, Type.VOID_TYPE, new Type[]{ type }), null);
                    load_this();
                    load_arg(0);
                    putfield(fieldName);
                    return_value();
                }
                super.end_class();
            }
        };
    }
}
