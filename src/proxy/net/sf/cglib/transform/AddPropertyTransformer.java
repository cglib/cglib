package net.sf.cglib.transform;

import net.sf.cglib.core.*;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

public class AddPropertyTransformer extends EmittingTransformer {
    private final String[] names;
    private final Type[] types;

    public AddPropertyTransformer(String[] names, Type[] types) {
        this.names = names;
        this.types = types;
    }

    protected Emitter getEmitter(ClassVisitor cv) {
        return new Emitter(cv) {
            public void end_class() {
                if (!TypeUtils.isAbstract(getClassAccess())) {
                    Type[] T = new Type[1];
                    for (int i = 0; i < names.length; i++) {
                        String fieldName = "$cglib_prop_" + names[i];
                        declare_field(Constants.ACC_PRIVATE, fieldName, types[i], null);

                        String property = TypeUtils.upperFirst(names[i]);
                        begin_method(Constants.ACC_PUBLIC,
                                     new Signature("get" + property,
                                                   types[i],
                                                   Constants.TYPES_EMPTY),
                                     null);
                        load_this();
                        getfield(fieldName);
                        return_value();

                        T[0] = types[i];
                        begin_method(Constants.ACC_PUBLIC,
                                     new Signature("set" + property,
                                                   Type.VOID_TYPE,
                                                   T),
                                     null);
                        load_this();
                        load_arg(0);
                        putfield(fieldName);
                        return_value();
                    }
                }
                super.end_class();
            }
        };
    }
}
