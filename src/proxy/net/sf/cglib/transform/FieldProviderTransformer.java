package net.sf.cglib.transform;

import java.util.*;
import net.sf.cglib.core.*;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public class FieldProviderTransformer extends EmittingTransformer {
    private static final Type FIELD_PROVIDER =
      TypeUtils.parseType("net.sf.cglib.transform.FieldProvider");
    private static final Type ILLEGAL_ARGUMENT_EXCEPTION =
      TypeUtils.parseType("IllegalArgumentException");
    private static final Signature PROVIDER_GET =
      TypeUtils.parseSignature("Object getField(String)");
    private static final Signature PROVIDER_SET =
      TypeUtils.parseSignature("void setField(String, Object)");

    private Map fields;
    
    protected Emitter getEmitter(ClassVisitor cv) {
        return new Emitter(cv) {
            public void begin_class(int access, String className, Type superType, Type[] interfaces, String sourceFile) {
                if (!TypeUtils.isAbstract(access)) {
                    interfaces = TypeUtils.add(interfaces, FIELD_PROVIDER);
                }
                fields = new HashMap();
                super.begin_class(access, className, superType, interfaces, sourceFile);
            }

            public void declare_field(int access, String name, Type type, Object value) {
                super.declare_field(access, name, type, value);
                fields.put(name, type);
            }

            public void end_class() {
                if (fields.size() == 0) {
                    super.end_class();
                    return;
                }
                String[] names = (String[])fields.keySet().toArray(new String[fields.size()]);
                try {
                    begin_method(Constants.ACC_PUBLIC, PROVIDER_GET, null);
                    load_this();
                    load_arg(0);
                    // TODO: if this is used to enhance class files SWITCH_STYLE_TRIE should be used
                    // to avoid JVM hashcode implementation incompatibilities
                    ComplexOps.string_switch(this, names, Constants.SWITCH_STYLE_HASH, new ObjectSwitchCallback() {
                        public void processCase(Object key, Label end) {
                            Type type = (Type)fields.get(key);
                            getfield((String)key);
                            box(type);
                            return_value();
                        }
                        public void processDefault() {
                            throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Unknown field name");
                        }
                    });

                    begin_method(Constants.ACC_PUBLIC, PROVIDER_SET, null);
                    load_this();
                    load_arg(1);
                    load_arg(0);
                    ComplexOps.string_switch(this, names, Constants.SWITCH_STYLE_HASH, new ObjectSwitchCallback() {
                        public void processCase(Object key, Label end) {
                            Type type = (Type)fields.get(key);
                            unbox(type);
                            putfield((String)key);
                            return_value();
                        }
                        public void processDefault() {
                            throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Unknown field name");
                        }
                    });
                } catch (Exception e) {
                    throw new CodeGenerationException(e);
                }
                super.end_class();
            }
        };
    }
}
