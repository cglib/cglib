package net.sf.cglib.transform;

import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;

public class AccessFieldTransformer extends EmittingTransformer {
    private Callback callback;

    public AccessFieldTransformer(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        String getPropertyName(Type owner, String fieldName);
    }

    public void declare_field(int access, final String name, Type type, Object value) {
        super.declare_field(access, name, type, value);

        String property = TypeUtils.upperFirst(callback.getPropertyName(getClassType(), name));
        if (property != null) {
            CodeEmitter e;
            e = begin_method(Constants.ACC_PUBLIC,
                             new Signature("get" + property,
                                           type,
                                           Constants.TYPES_EMPTY),
                             null);
            e.load_this();
            e.getfield(name);
            e.return_value();
            e.end_method();

            e = begin_method(Constants.ACC_PUBLIC,
                             new Signature("set" + property,
                                           Type.VOID_TYPE,
                                           new Type[]{ type }),
                             null);
            e.load_this();
            e.load_arg(0);
            e.putfield(name);
            e.return_value();
            e.end_method();
        }
    }
}
