package net.sf.cglib.transform;

import java.lang.reflect.*;
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;

/**
 * @author Juozas Baliuka
 */
public class AddDelegateTransformer extends EmittingTransformer {
    
    private static final String DELEGATE = "$CGLIB_DELEGATE";
    private static final Signature CSTRUCT_OBJECT =
      TypeUtils.parseSignature("void <init>(Object)");
    
    private Class[] delegateIf;
    private Class delegateImpl;
    private Type delegateType;
    
    /** Creates a new instance of AddDelegateTransformer */
    public AddDelegateTransformer(Class delegateIf[], Class delegateImpl) {
        try {
            delegateImpl.getConstructor(new Class[]{ Object.class });
            this.delegateIf = delegateIf;
            this.delegateImpl = delegateImpl;
            delegateType = Type.getType(delegateImpl);
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }
    
    protected Emitter getEmitter(ClassVisitor cv) {
        return new Emitter(cv) {
            public void begin_class(int access, String className, Type superType, Type[] interfaces, String sourceFile) {
                Type[] all = TypeUtils.add(interfaces, TypeUtils.getTypes(delegateIf));
                super.begin_class(access, className, superType, all, sourceFile);
                declare_field(Constants.ACC_PRIVATE | Constants.ACC_TRANSIENT,
                              DELEGATE,
                              delegateType,
                              null);
                for (int i = 0; i < delegateIf.length; i++) {
                    Method[] methods = delegateIf[i].getMethods();
                    for (int j = 0; j < methods.length; j++) {
                        if (Modifier.isAbstract(methods[j].getModifiers())) {
                            addDelegate(methods[j]);
                        }
                    }
                }
            }

            public CodeVisitor begin_method(int access, Signature sig, Type[] exceptions) {
                CodeVisitor v = super.begin_method(access, sig, exceptions);
                if (sig.getName().equals(Constants.CONSTRUCTOR_NAME)) {
                    return new CodeAdapter(v) {
                        private boolean transformInit = true;
                        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                            super.visitMethodInsn(opcode, owner, name, desc);
                            if (transformInit && opcode == Constants.INVOKESPECIAL) {
                                load_this();
                                new_instance(delegateType);
                                dup();
                                load_this();
                                invoke_constructor(delegateType, CSTRUCT_OBJECT);
                                putfield(DELEGATE);
                                transformInit = false;
                            }
                        }
                    };
                }
                return v;
            }

            private void addDelegate(Method m) {
                Method delegate;
                try {
                    delegate = delegateImpl.getMethod(m.getName(), m.getParameterTypes());
                    if (!delegate.getReturnType().getName().equals(m.getReturnType().getName())){
                        throw new IllegalArgumentException("Invalid delegate signature " + delegate);
                    }
                } catch (NoSuchMethodException e) {
                    throw new CodeGenerationException(e);
                }

                Signature sig = ReflectUtils.getSignature(m);
                begin_method(Constants.ACC_PUBLIC, sig, TypeUtils.getTypes(m.getExceptionTypes()));
                load_this();
                getfield(DELEGATE);
                load_args();
                invoke_virtual(delegateType, sig);
                return_value();
            }
        };
    }
}



