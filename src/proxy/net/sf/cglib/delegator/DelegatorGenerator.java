package net.sf.cglib.delegator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import net.sf.cglib.util.*;
import net.sf.cglib.util.MethodWrapper;

/* package */ class DelegatorGenerator extends CodeGenerator implements ClassFileConstants {
    private static final String FIELD_NAME = "CGLIB$DELEGATES";

    private Class[] interfaces;
        
    /* package */ DelegatorGenerator(String className, Class[] interfaces, ClassLoader loader) {
        super(className, Object.class, loader);
        this.interfaces = interfaces;
    }

    protected void generate() throws NoSuchMethodException {
        declare_interfaces(interfaces);
        declare_interface(Delegator.Factory.class);
        generateConstructor();
        generateFactory();

        Set methodSet = new HashSet();
        for (int i = 0; i < interfaces.length; i++) {
            Class iface = interfaces[i];
            if (!iface.isInterface()) {
                throw new IllegalArgumentException(iface + " is not an interface");
            }
            Method[] methods = iface.getMethods();
            for (int j = 0; j < methods.length; j++) {
                Method method = methods[j];
                Object methodKey = MethodWrapper.newInstance(method);
                if (!methodSet.contains(methodKey)) {
                    methodSet.add(methodKey);
                    generateProxy(iface, method, i);
                }
            }
        }
    }

    private void generateConstructor() {
        declare_field(Modifier.PRIVATE, OBJECT_ARRAY_CLASS, FIELD_NAME);
        begin_constructor(OBJECT_ARRAY_CLASS_ARRAY);
        load_this();
        super_invoke_constructor();
        load_this();
        load_arg(0);
        putfield(FIELD_NAME);
        return_value();
        end_constructor();
    }

    private void generateFactory() throws NoSuchMethodException {
        Method newInstance =
            Delegator.Factory.class.getMethod("cglib_newInstance", OBJECT_ARRAY_CLASS_ARRAY);
        begin_method(newInstance);
        new_instance_this();
        dup();
        load_arg(0);
        invoke_constructor_this(OBJECT_ARRAY_CLASS_ARRAY);
        return_value();
        end_method();
    }

    private void generateProxy(Class iface, Method method, int arrayref) {
        begin_method(method);
        load_this();
        getfield(FIELD_NAME);
        aaload(arrayref);
        checkcast(iface);
        load_args();
        invoke(method);
        return_value();
        end_method();
    }
}
