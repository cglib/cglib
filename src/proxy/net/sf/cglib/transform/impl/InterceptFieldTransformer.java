package net.sf.cglib.transform.impl;

import net.sf.cglib.transform.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class InterceptFieldTransformer extends EmittingTransformer {
    private static final String CALLBACK_FIELD = "$CGLIB_READ_WRITE_CALLBACK";
    private static final Type CALLBACK =
      TypeUtils.parseType("net.sf.cglib.transform.impl.InterceptFieldCallback");
    private static final Type ENABLED =
      TypeUtils.parseType("net.sf.cglib.transform.impl.InterceptFieldEnabled");
    private static final Signature ENABLED_SET =
      TypeUtils.parseSignature("void setInterceptFieldCallback(net.sf.cglib.transform.impl.InterceptFieldCallback)");
    private static final Signature ENABLED_GET =
      TypeUtils.parseSignature("net.sf.cglib.transform.impl.InterceptFieldCallback getInterceptFieldCallback()");

    private InterceptFieldFilter filter;
    
    public InterceptFieldTransformer(InterceptFieldFilter filter) {
        this.filter = filter;
    }
    
    public void begin_class(int access, String className, Type superType, Type[] interfaces, String sourceFile) {
        if (!TypeUtils.isInterface(access)) {
            super.begin_class(access, className, superType, TypeUtils.add(interfaces, ENABLED), sourceFile);
                    
            super.declare_field(Constants.ACC_PRIVATE | Constants.ACC_TRANSIENT,
                                CALLBACK_FIELD,
                                CALLBACK,
                                null,
                                null);

            CodeEmitter e;
            e = super.begin_method(Constants.ACC_PUBLIC, ENABLED_GET, null, null);
            e.load_this();
            e.getfield(CALLBACK_FIELD);
            e.return_value();
            e.end_method();
                
            e = super.begin_method(Constants.ACC_PUBLIC, ENABLED_SET, null, null);
            e.load_this();
            e.load_arg(0);
            e.putfield(CALLBACK_FIELD);
            e.return_value();
            e.end_method();
        } else {
            super.begin_class(access, className, superType, interfaces, sourceFile);
        }
    }

    public void declare_field(int access, String name, Type type, Object value, Attribute attrs) {
        super.declare_field(access, name, type, value, attrs);
        if (!TypeUtils.isStatic(access)) {
            if (filter.acceptRead(getClassType(), name)) {
                addReadMethod(name, type);
            }
            if (filter.acceptWrite(getClassType(), name)) {
                addWriteMethod(name, type);
            }
        }
    }

    private void addReadMethod(String name, Type type) {
        CodeEmitter e = super.begin_method(Constants.ACC_PUBLIC,
                                           readMethodSig(name, type.getDescriptor()),
                                           null,
                                           null);
        e.load_this();
        e.getfield(name);
        e.load_this();
        e.getfield(CALLBACK_FIELD);
        Label intercept = e.make_label();
        e.ifnonnull(intercept);
        e.return_value();

        e.mark(intercept);
        Local result = e.make_local(type);
        e.store_local(result);
        e.load_this();
        e.getfield(CALLBACK_FIELD);
        e.load_this();
        e.push(name);
        e.load_local(result);
        e.invoke_interface(CALLBACK, readCallbackSig(type));
        if (!TypeUtils.isPrimitive(type)) {
            e.checkcast(type);
        }
        e.return_value();
        e.end_method();
    }

    private void addWriteMethod(String name, Type type) {
        CodeEmitter e = super.begin_method(Constants.ACC_PUBLIC,
                                           writeMethodSig(name, type.getDescriptor()),
                                           null,
                                           null);
        e.load_this();
        e.dup();
        e.getfield(CALLBACK_FIELD);
        Label skip = e.make_label();
        e.ifnull(skip);

        e.load_this();
        e.getfield(CALLBACK_FIELD);
        e.load_this();
        e.push(name);
        e.load_this();
        e.getfield(name);
        e.load_arg(0);
        e.invoke_interface(CALLBACK, writeCallbackSig(type));
        if (!TypeUtils.isPrimitive(type)) {
            e.checkcast(type);
        }
        Label go = e.make_label();
        e.goTo(go);
        e.mark(skip);
        e.load_arg(0);
        e.mark(go);
        e.putfield(name);
        e.return_value();
        e.end_method();
    }
                
    public CodeEmitter begin_method(int access, Signature sig, Type[] exceptions, Attribute attrs) {
        return new CodeEmitter(super.begin_method(access, sig, exceptions, attrs)) {
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                Type towner = TypeUtils.fromInternalName(owner);
                switch (opcode) {
                case Constants.GETFIELD:
                    if (filter.acceptRead(towner, name)) {
                        helper(towner, readMethodSig(name, desc));
                        return;
                    }
                    break;
                case Constants.PUTFIELD:
                    if (filter.acceptWrite(towner, name)) {
                        helper(towner, writeMethodSig(name, desc));
                        return;
                    }
                    break;
                }
                super.visitFieldInsn(opcode, owner, name, desc);
            }

            private void helper(Type owner, Signature sig) {
                invoke_virtual(owner, sig);
            }
        };
    }

    private static Signature readMethodSig(String name, String desc) {
        return new Signature("$cglib_read_" + name, "()" + desc);
    }

    private static Signature writeMethodSig(String name, String desc) {
        return new Signature("$cglib_write_" + name, "(" + desc + ")V");
    }

    private static Signature readCallbackSig(Type type) {
        Type remap = remap(type);
        return new Signature("read" + callbackName(remap),
                             remap,
                             new Type[]{ Constants.TYPE_OBJECT,
                                         Constants.TYPE_STRING,
                                         remap });
    }

    private static Signature writeCallbackSig(Type type) {
        Type remap = remap(type);
        return new Signature("write" + callbackName(remap),
                             remap,
                             new Type[]{ Constants.TYPE_OBJECT,
                                         Constants.TYPE_STRING,
                                         remap,
                                         remap });
    }

    private static Type remap(Type type) {
        switch (type.getSort()) {
        case Type.OBJECT:
        case Type.ARRAY:
            return Constants.TYPE_OBJECT;
        default:
            return type;
        }
    }

    private static String callbackName(Type type) {
        return (type == Constants.TYPE_OBJECT) ?
            "Object" :
            TypeUtils.upperFirst(TypeUtils.getClassName(type));
    }
}
