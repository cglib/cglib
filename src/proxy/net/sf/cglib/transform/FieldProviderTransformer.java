package net.sf.cglib.transform;

import java.util.*;
import net.sf.cglib.core.*;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
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
                if( (access & Constants.ACC_STATIC) == 0 ){
                    fields.put(name, type);
                }
            }

             public CodeVisitor begin_method(int access, Signature sig, Type[] exceptions) {
                CodeVisitor v = super.begin_method(access, sig, exceptions);
                if (sig.getName().equals(Constants.STATIC_NAME)) {
                    generatedClassInit = true;
                    invoke_static(getClassType(),INIT_FIELD_PROVIDER);
                    
                }
                return v;
            }

            
            public void end_class() {
                if (fields.size() == 0) {
                    super.end_class();
                    return;
                }
               final String[] names = (String[])fields.keySet().toArray(new String[fields.size()]);
               
               int indexes[] = new int[names.length];
               for(int i = 0; i < indexes.length; i++ ){ indexes[i] = i; }
                    
                try {
                    
                    if( !generatedClassInit ){
                        
                      begin_static();
                      return_value();
                    
                    }
                    
                    
                    super.declare_field(
                                         Constants.ACC_PRIVATE|Constants.ACC_STATIC,
                                         FIELD_NAMES, 
                                         Type.getType( String[].class ), null 
                               );
                    super.declare_field(
                                         Constants.ACC_PRIVATE|Constants.ACC_STATIC,
                                         FIELD_TYPES, 
                                         Type.getType( Class[].class ), null 
                               );
              
                    
                    begin_method(Constants.ACC_STATIC,INIT_FIELD_PROVIDER,null);
                    push(names.length);
                    newarray(Type.getType(String.class));
                    dup();
                    
                    for(int i = 0; i < names.length; i++ ){ 
                       dup();
                       push(i);
                       push(names[i]);
                       aastore();
                    }
                    
                    putstatic(getClassType(),FIELD_NAMES, Type.getType( String[].class )); 
                    
                    push(names.length);
                    newarray(Type.getType(Class.class));
                    dup();
                    
                    for(int i = 0; i < names.length; i++ ){ 
                       dup();
                       push(i);
                       Type type = (Type)fields.get(names[i]);
                       ComplexOps.load_class( this, type );
                       aastore();
                    }
                    
                    putstatic(getClassType(),FIELD_TYPES, Type.getType( Class[].class )); 
                    return_value();
                    
                    begin_method(Constants.ACC_PUBLIC, PROVIDER_GET_NAMES, null);
                    getstatic(getClassType(), FIELD_NAMES, Type.getType(String[].class) );
                    return_value();
                    
                    begin_method(Constants.ACC_PUBLIC, PROVIDER_GET_TYPES, null);
                    getstatic(getClassType(), FIELD_TYPES, Type.getType(Class[].class) );
                    return_value();
                    
                    
                    begin_method(Constants.ACC_PUBLIC, PROVIDER_SET_BY_INDEX, null);
                    load_this();
                    load_arg(1);
                    load_arg(0);
                  
                    
                    process_switch( indexes, new ProcessSwitchCallback(){
                        
                          public void processCase(int key, Label end) throws Exception{
                          
                              Type type = (Type)fields.get(names[key]);
                               unbox(type);
                               putfield(names[key]);
                               return_value();
                          
                          }
                          public void processDefault() throws Exception{
                             throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Unknown field index");         
                          }

                    }
                    );
                    
                    
                    
                    begin_method(Constants.ACC_PUBLIC, PROVIDER_GET_BY_INDEX, null);
                    load_this();
                    load_arg(0);
                    
                    process_switch( indexes,new ProcessSwitchCallback(){
                        
                          public void processCase(int key, Label end) throws Exception{
                          
                              Type type = (Type)fields.get(names[key]);
                               getfield(names[key]);
                               box(type);
                               return_value();
                          
                          }
                          public void processDefault() throws Exception{
                             throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Unknown field index");         
                          }

                    }
                    );
                    
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
