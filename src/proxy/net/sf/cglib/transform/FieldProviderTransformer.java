package net.sf.cglib.transform;

import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public class FieldProviderTransformer extends EmittingTransformer {
    
    private static final String FIELD_NAMES = "CGLIB$FIELD_NAMES";
    private static final String FIELD_TYPES = "CGLIB$FIELD_TYPES";
    
    private static final Type FIELD_PROVIDER =
      TypeUtils.parseType("net.sf.cglib.transform.FieldProvider");
    private static final Type ILLEGAL_ARGUMENT_EXCEPTION =
      TypeUtils.parseType("IllegalArgumentException");
    private static final Signature PROVIDER_GET =
      TypeUtils.parseSignature("Object getField(String)");
    private static final Signature PROVIDER_SET =
      TypeUtils.parseSignature("void setField(String, Object)");
    private static final Signature PROVIDER_SET_BY_INDEX =
      TypeUtils.parseSignature("void setField(int, Object)");
    private static final Signature PROVIDER_GET_BY_INDEX =
      TypeUtils.parseSignature("Object getField(int)");
    private static final Signature PROVIDER_GET_TYPES =
      TypeUtils.parseSignature("Class[] getFieldTypes()");
    private static final Signature PROVIDER_GET_NAMES =
      TypeUtils.parseSignature("String[] getFieldNames()");
    private static final Signature INIT_FIELD_PROVIDER =
      TypeUtils.parseSignature("void cglib$initFieldProvider()");
    
    private boolean generatedClassInit = false;
    private Map fields;
    
    public void begin_class(int access, String className, Type superType, Type[] interfaces, String sourceFile) {
        if (!TypeUtils.isAbstract(access)) {
            interfaces = TypeUtils.add(interfaces, FIELD_PROVIDER);
            fields = new HashMap();
        }
        super.begin_class(access, className, superType, interfaces, sourceFile);
    }

    public void declare_field(int access, String name, Type type, Object value) {
        super.declare_field(access, name, type, value);
        if (fields != null && !TypeUtils.isStatic(access)) {
            fields.put(name, type);
        }
    }

    public CodeEmitter begin_method(int access, Signature sig, Type[] exceptions) {
        CodeEmitter e = super.begin_method(access, sig, exceptions);
        if (sig.getName().equals(Constants.STATIC_NAME)) {
            generatedClassInit = true;
            e.invoke_static(getClassType(), INIT_FIELD_PROVIDER);
        }
        return e;
    }

    public void end_class() {
        if (!generatedClassInit) {
            CodeEmitter e = begin_static();
            e.return_value();
            e.end_method();
        }
        try {
            if (fields != null) {
                generate();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
        super.end_class();
    }

    private void generate() throws Exception {
        final String[] names = (String[])fields.keySet().toArray(new String[fields.size()]);

        int indexes[] = new int[names.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        
        super.declare_field(Constants.PRIVATE_FINAL_STATIC, FIELD_NAMES, Constants.TYPE_STRING_ARRAY, null);
        super.declare_field(Constants.PRIVATE_FINAL_STATIC, FIELD_TYPES, Constants.TYPE_CLASS_ARRAY, null);

        // use separate methods here because each process switch inner class needs a final CodeEmitter
        initFieldProvider(names);
        getNames();
        getTypes();
        getField(names);
        setField(names);
        setByIndex(names, indexes);
        getByIndex(names, indexes);
    }

    private void initFieldProvider(String[] names) {
        CodeEmitter e = super.begin_method(Constants.ACC_STATIC, INIT_FIELD_PROVIDER, null);
        ComplexOps.push_object(e, names);
        e.putstatic(getClassType(), FIELD_NAMES, Constants.TYPE_STRING_ARRAY);
        
        e.push(names.length);
        e.newarray(Constants.TYPE_CLASS);
        e.dup();
        for(int i = 0; i < names.length; i++ ){ 
            e.dup();
            e.push(i);
            Type type = (Type)fields.get(names[i]);
            ComplexOps.load_class(e, type);
            e.aastore();
        }
        e.putstatic(getClassType(), FIELD_TYPES, Constants.TYPE_CLASS_ARRAY);
        e.return_value();
        e.end_method();
    }

    private void getNames() {
        CodeEmitter e = super.begin_method(Constants.ACC_PUBLIC, PROVIDER_GET_NAMES, null);
        e.getstatic(getClassType(), FIELD_NAMES, Constants.TYPE_STRING_ARRAY);
        e.return_value();
        e.end_method();
    }

    private void getTypes() {
        CodeEmitter e = super.begin_method(Constants.ACC_PUBLIC, PROVIDER_GET_TYPES, null);
        e.getstatic(getClassType(), FIELD_TYPES, Constants.TYPE_CLASS_ARRAY);
        e.return_value();
        e.end_method();
    }

    private void setByIndex(final String[] names, final int[] indexes) throws Exception {
        final CodeEmitter e = super.begin_method(Constants.ACC_PUBLIC, PROVIDER_SET_BY_INDEX, null);
        e.load_this();
        e.load_arg(1);
        e.load_arg(0);
        e.process_switch(indexes, new ProcessSwitchCallback() {
            public void processCase(int key, Label end) throws Exception {
                Type type = (Type)fields.get(names[key]);
                e.unbox(type);
                e.putfield(names[key]);
                e.return_value();
            }
            public void processDefault() throws Exception {
                e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Unknown field index");         
            }
        });
        e.end_method();
    }

    private void getByIndex(final String[] names, final int[] indexes) throws Exception {
        final CodeEmitter e = super.begin_method(Constants.ACC_PUBLIC, PROVIDER_GET_BY_INDEX, null);
        e.load_this();
        e.load_arg(0);
        e.process_switch(indexes, new ProcessSwitchCallback() {
            public void processCase(int key, Label end) throws Exception {
                Type type = (Type)fields.get(names[key]);
                e.getfield(names[key]);
                e.box(type);
                e.return_value();
            }
            public void processDefault() throws Exception {
                e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Unknown field index");         
            }
        });
        e.end_method();
    }

    // TODO: if this is used to enhance class files SWITCH_STYLE_TRIE should be used
    // to avoid JVM hashcode implementation incompatibilities
    private void getField(String[] names) throws Exception {
        final CodeEmitter e = begin_method(Constants.ACC_PUBLIC, PROVIDER_GET, null);
        e.load_this();
        e.load_arg(0);
        ComplexOps.string_switch(e, names, Constants.SWITCH_STYLE_HASH, new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
                Type type = (Type)fields.get(key);
                e.getfield((String)key);
                e.box(type);
                e.return_value();
            }
            public void processDefault() {
                e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Unknown field name");
            }
        });
        e.end_method();
    }

    private void setField(String[] names) throws Exception {
        final CodeEmitter e = begin_method(Constants.ACC_PUBLIC, PROVIDER_SET, null);
        e.load_this();
        e.load_arg(1);
        e.load_arg(0);
        ComplexOps.string_switch(e, names, Constants.SWITCH_STYLE_HASH, new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
                Type type = (Type)fields.get(key);
                e.unbox(type);
                e.putfield((String)key);
                e.return_value();
            }
            public void processDefault() {
                e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Unknown field name");
            }
        });
        e.end_method();
    }
}
