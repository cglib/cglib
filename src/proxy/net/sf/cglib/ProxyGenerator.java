/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package net.sf.cglib;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ProxyGenerator extends CodeGenerator {
    private static final String FIELD_NAME = "CGLIB$HANDLER";
    private Class[] interfaces = null;
    private Map methodMap = null; //method name + method signature -> method object
    private Map interfaceMap = null; //method name + method signature -> interface object
    private Map exceptionMap = null; //method name + method signature -> exception list String[]
    private String indent = "";

    public ProxyGenerator(String className, Class[] interfaces, ClassLoader loader) throws ClassNotFoundException {
        super(className, Proxy.class, loader);
        //super.setDebugLocation("debug.txt");
        super.setDebug(true);
        if (isDebug()) debug(className + " implements");
        if (isDebug()) increaseIndent(4);
        String[] interfacesStr = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfacesStr[i] = interfaces[i].getName();
            if (isDebug()) debug(interfacesStr[i]);
        }
        if (isDebug()) decreaseIndent(4);
        if (isDebug()) debug("{ // " + className);
        this.interfaces = interfaces;
        methodMap = new HashMap(interfaces.length * 2 + 5);
        exceptionMap = new HashMap(interfaces.length * 2 + 5);
        interfaceMap = new HashMap(interfaces.length * 2 + 5);
        if (isDebug()) debug("} // " + className + "\n");
    }
    
    protected void generate() throws NoSuchMethodException, NoSuchFieldException {
        declare_interfaces(interfaces);
        declare_field(Modifier.PRIVATE, InvocationHandler.class, FIELD_NAME);
        generateStaticClassFields(this.interfaces);
        generateConstructor();

        inspectMethods(this.interfaces);
        Iterator methods =  methodMap.keySet().iterator();
        for (Iterator iter = methodMap.keySet().iterator(); iter.hasNext();) {
            generateMethod((Method) methodMap.get(iter.next()));
        }
    }

    // java.lang.Object special case: hashCode(), equals() and toString() will be proxied
    private static final Set proxiedMethodsOnObject = new HashSet(5);
    static {
        proxiedMethodsOnObject.add("hashCode()I");
        proxiedMethodsOnObject.add("equals(Ljava/lang/Object;)Z");
        proxiedMethodsOnObject.add("toString()Ljava/lang/String;");
    }

    private void inspectMethods(Class[] interfaces) {
        Class o = Object.class;
        Method[] oMethods = o.getMethods();
        for (int j = 0; j < oMethods.length; j++) {
            String sig = getMethodSignature(oMethods[j]);
            if (proxiedMethodsOnObject.contains(sig) && Modifier.isPublic(oMethods[j].getModifiers())) 
                inspectMethod(o, oMethods[j]);
        }
        for (int i = 0; i < interfaces.length; i++) {
            if (isDebug()) debug("//interface: " + interfaces[i].getName());
            Method[] methods = interfaces[i].getMethods();
            for (int j = 0; j < methods.length; j++) {
                inspectMethod(interfaces[i], methods[j]);
            }
        }
    }

    private void inspectMethod(Class clazz, Method method) {
        //pseudo-signature
        String sig = getMethodSignature(method);
        Class[] et = method.getExceptionTypes();
        if (isDebug()) debug("//method: " + method);
        if (!methodMap.containsKey(sig)) {
            methodMap.put(sig, method);
            interfaceMap.put(sig, clazz);
            Set exceptionSet;
            if (et != null) {
                exceptionSet = new HashSet(et.length * 2  + 5);
                exceptionSet.addAll(Arrays.asList(et));
            } else{
                exceptionSet = new HashSet(5);
            }
            exceptionMap.put(sig, exceptionSet);
        } else {
            // if duplicate method signatures, then the exceptions thrown by the method is a union of the exceptions 
            // thrown by these duplicate methods
            if (et != null) {
                Set exceptionSet = (Set) exceptionMap.get(sig);
                exceptionSet.addAll(Arrays.asList(et));
            }
        }
    }

    private void generateConstructor() {
        Class[] argTypes = new Class[] { InvocationHandler.class };
        begin_constructor(argTypes);
        load_this();
        load_arg(0);
        super_invoke_constructor(argTypes);
        load_this();
        load_arg(0);
        putfield(FIELD_NAME);
        return_value();
        end_method();
    }

    protected String getInvocationHandlerMethodName() {
        return InvocationHandler.class.getName() + ".invoke(Object,Method,Object[])";
    }

    protected Class getUndeclaredThrowableExceptionClass() {
        return UndeclaredThrowableException.class;
    }

    private void generateMethod(Method method) throws NoSuchMethodException, NoSuchFieldException {
        String methodSignature = getMethodSignature(method);
        if (isDebug()) debug("//method: " + methodSignature);
        Set exceptionSet = (Set) exceptionMap.get(methodSignature);
        Class[] exceptions = (Class[]) exceptionSet.toArray(new Class[exceptionSet.size()]);
        Class[] argumentTypes = method.getParameterTypes();
        String[] argumentNames = new String[argumentTypes.length];
        Class returnType = method.getReturnType();
        Class resultType = null;
        if (returnType.isPrimitive()) {
            resultType = getPrimitiveWrapperClass(returnType);
        } else if (!returnType.equals(Void.TYPE)) {
            resultType = returnType;
        }
        Class interfaceClass = (Class) interfaceMap.get(methodSignature);

        if (isDebug()) increaseIndent();
        begin_method(method, Modifier.PUBLIC | Modifier.FINAL, exceptions);
        Object localVariable = null;
        if (resultType != null) {
            localVariable = make_local(resultType);
            if (isDebug()) debug(resultType + " " + localVariable + " = null;");
            aconst_null();
            store_local(localVariable);
        }

        //load handler
        Object start_try = begin_handler();
        load_this();
        getfield(FIELD_NAME);
        load_this();
        //load method interface class
        load_class(interfaceClass);
        //loadObjectClass(interfaceClass);
        //add method name
        push(method.getName());

        //create array of argument types
        if (argumentTypes.length > 0) {
            if (isDebug()) debug("Class[] argumentTypes = new Class[" + argumentTypes.length + "];");
            create_arg_type_array();
        } else {
            if (isDebug()) debug("Class[] argumentTypes = null;");
            aconst_null();
        }

        //get the method object to be passed to delegate, using reflection API
        invoke(MethodConstants.CLASS_GET_METHOD);

        //create new array of argument values to be passed to delegate
        if (argumentNames.length > 0) {
            if (isDebug()) debug("Object[] arguments = new Object[" + argumentNames.length + "];");
            create_arg_array();
        } else {
            if (isDebug()) debug("Class[] arguments = null;");
            aconst_null();
        }

        //invoke the delegator method
        if (isDebug()) debug("handler.invoke();");
        invoke(ReflectUtils.findMethod(getInvocationHandlerMethodName()));

        if (resultType != null) {
            //save the result
            if (isDebug()) debug("//check cast: " + resultType);
            checkcast(resultType);
            if (isDebug()) debug("//store to result");
            //store_local(localVariableName);
        }
        String ok = anon_label();
        goTo(ok);
        end_handler();

        //standard catch blocks
        generateReThrow(start_try, ClassCastException.class);
        generateNestedThrow(start_try, NoSuchMethodException.class, Error.class);
        generateReThrow(start_try, RuntimeException.class);

        //declared exception catch blocks, will be re-thrown
        for (int i = 0; i < exceptions.length; i++) {
            generateReThrow(start_try, exceptions[i]);
        }

        //standard catch blocks, continued...
        generateReThrow(start_try, Error.class);
        generateNestedThrow(start_try, Throwable.class, getUndeclaredThrowableExceptionClass());

        nop(ok);
        //load the result and return it, or, just return
        if (resultType != null) {
            //load_local(localVariableName);
            if (returnType.isPrimitive()) {
                // primitive type
                if (isDebug()) debug("return primitive " + localVariable +  ";");
                invoke(getPrimitiveMethod(returnType));
            } else {
                // object
                if (isDebug()) debug("return " + localVariable +  ";");
            }
        } else {
            // void
            if (isDebug()) debug("return;");
        }
        return_value();
        end_method();
        if (isDebug()) decreaseIndent();
        if (isDebug()) debug("} //" + methodSignature + "\n");
    }

    protected String getMethodSignature(Method method) {
        return method.getName() + getMethodSignature(method.getReturnType(), method.getParameterTypes());
    }

    private void debug() {
        System.out.println();
    }

    private void debug(String msg) {
        System.out.println(this.indent + msg);
    }
    
    private void increaseIndent() {
        increaseIndent(1);
    }
    
    private void increaseIndent(int n) {
        if (n > 1) {
            StringBuffer sb = new StringBuffer(this.indent.length() + n * 4);
            sb.append(this.indent);
            for (int i = 0; i < n; i++) sb.append("    ");
            this.indent = sb.toString();
        } else {
            this.indent += "    ";
        }
    }

    private void decreaseIndent() {
        decreaseIndent(1);
    }
    
    private void decreaseIndent(int n) {
        this.indent = this.indent.substring(4*n);
    }

    private boolean isDebug() {
        return true;
    }

}
