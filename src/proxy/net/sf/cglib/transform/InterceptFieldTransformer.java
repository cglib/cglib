package net.sf.cglib.transform;

import net.sf.cglib.core.*;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class InterceptFieldTransformer extends EmittingTransformer {
    private static final String CALLBACK_FIELD = "$CGLIB_READ_WRITE_CALLBACK";
    private static final Type CALLBACK =
      TypeUtils.parseType("net.sf.cglib.transform.InterceptFieldCallback");
    private static final Type ENABLED =
      TypeUtils.parseType("net.sf.cglib.transform.InterceptFieldEnabled");
    private static final Signature ENABLED_SET =
      TypeUtils.parseSignature("void setInterceptFieldCallback(net.sf.cglib.transform.InterceptFieldCallback)");
    private static final Signature ENABLED_GET =
      TypeUtils.parseSignature("net.sf.cglib.transform.InterceptFieldCallback getInterceptFieldCallback()");

    private InterceptFieldFilter filter;
    
    public InterceptFieldTransformer(InterceptFieldFilter filter) {
        this.filter = filter;
    }
    
    protected Emitter getEmitter(ClassVisitor cv) {
        return new Emitter(cv) {
            public void begin_class(int access, String className, Type superType, Type[] interfaces, String sourceFile) {
                if (!TypeUtils.isAbstract(access)) {
                    super.begin_class(access, className, superType, TypeUtils.add(interfaces, ENABLED), sourceFile);
                    super.declare_field(Constants.ACC_PRIVATE | Constants.ACC_TRANSIENT,
                                        CALLBACK_FIELD,
                                        CALLBACK,
                                        null);

                    begin_method(Constants.ACC_PUBLIC, ENABLED_GET, null);
                    load_this();
                    getfield(CALLBACK_FIELD);
                    return_value();
                
                    begin_method(Constants.ACC_PUBLIC, ENABLED_SET, null);
                    load_this();
                    load_arg(0);
                    putfield(CALLBACK_FIELD);
                    return_value();
                } else {
                    super.begin_class(access, className, superType, interfaces, sourceFile);
                }
            }

            public void declare_field(int access, String name, Type type, Object value) {
                super.declare_field(access, name, type, value);
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
                begin_method(Constants.ACC_PUBLIC, readMethodSig(name, type.getDescriptor()), null);
                load_this();
                getfield(name);
                load_this();
                getfield(CALLBACK_FIELD);
                Label intercept = make_label();
                ifnonnull(intercept);
                return_value();

                mark(intercept);
                Local result = make_local(type);
                store_local(result);
                load_this();
                getfield(CALLBACK_FIELD);
                load_this();
                push(name);
                load_local(result);
                invoke_interface(CALLBACK, readCallbackSig(type));
                if (!TypeUtils.isPrimitive(type)) {
                    checkcast(type);
                }
                return_value();
            }

            private void addWriteMethod(String name, Type type) {
                begin_method(Constants.ACC_PUBLIC, writeMethodSig(name, type.getDescriptor()), null);
                load_this();
                dup();
                getfield(CALLBACK_FIELD);
                Label skip = make_label();
                ifnull(skip);

                load_this();
                getfield(CALLBACK_FIELD);
                load_this();
                push(name);
                load_this();
                getfield(name);
                load_arg(0);
                invoke_interface(CALLBACK, writeCallbackSig(type));
                if (!TypeUtils.isPrimitive(type)) {
                    checkcast(type);
                }
                Label go = make_label();
                goTo(go);
                mark(skip);
                load_arg(0);
                mark(go);
                putfield(name);
                return_value();
            }
                
            public CodeVisitor begin_method(int access, Signature sig, Type[] exceptions) {
                return new CodeAdapter(super.begin_method(access, sig, exceptions)) {
                    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                        Type towner = TypeUtils.fromInternalName(owner);
                        switch (opcode) {
                        case Constants.GETFIELD:
                            if (filter.acceptRead(towner, name)) {
                                invoke_virtual(towner, readMethodSig(name, desc));
                                return;
                            }
                            break;
                        case Constants.PUTFIELD:
                            if (filter.acceptWrite(towner, name)) {
                                invoke_virtual(towner, writeMethodSig(name, desc));
                                return;
                            }
                            break;
                        }
                        super.visitFieldInsn(opcode, owner, name, desc);
                    }
                };
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
