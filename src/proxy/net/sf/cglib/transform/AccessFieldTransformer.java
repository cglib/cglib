package net.sf.cglib.transform;

import net.sf.cglib.core.*;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

public class AccessFieldTransformer extends EmittingTransformer {
    private Callback callback;

    public AccessFieldTransformer(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        String getPropertyName(Type owner, String fieldName);
    }

    protected Emitter getEmitter(ClassVisitor cv) {
        return new Emitter(cv) {
            public void declare_field(int access, String name, Type type, Object value) {
                super.declare_field(access, name, type, value);

                // TODO: what if we're inside another method already?
                String property = TypeUtils.upperFirst(callback.getPropertyName(getClassType(), name));
                if (property != null) {
                    begin_method(Constants.ACC_PUBLIC,
                                 new Signature("get" + property,
                                               type,
                                               Constants.TYPES_EMPTY),
                                 null);
                    load_this();
                    getfield(name);
                    return_value();

                    begin_method(Constants.ACC_PUBLIC,
                                 new Signature("set" + property,
                                               Type.VOID_TYPE,
                                               new Type[]{ type }),
                                 null);
                    load_this();
                    load_arg(0);
                    putfield(name);
                    return_value();
                }
            }
        };
    }
}
