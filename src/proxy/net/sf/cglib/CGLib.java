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

/**
 * public cglib utilities and shorcut methods
 * @author  baliuka
 */
public final class CGLib {
    
    
    private CGLib() {
    }
    
    /**
     *  implements decorator  for the first parameter,
     *  returned instance extends obj.getClass() and implements Factory interface,
     *  MethodProxy delegates calls to obj methods
     *  @param obj object to decorate
     *  @param interceptor interceptor used to handle implemented methods
     *  @return decorated instanse of obj.getClass()  class
     */
    
    
    public static Object decorate(Object obj, MethodInterceptor interceptor ){
      return Enhancer.decorate(obj, interceptor);
    }
    
    /**
     *  overrides Class methods and implements all abstract methods.  
     *  returned instance extends clazz and implements Factory interface,
     *  MethodProxy delegates calls to supper Class (clazz) methods, if not abstract.
     *  @param clazz Class or inteface to extend or implement
     *  @param interceptor interceptor used to handle implemented methods
     *  @return instanse of clazz class, new Class is defined in the same class loader
     */
    
     
      public static Object override(Class clazz, MethodInterceptor interceptor ){
          
         return Enhancer.override(clazz, interceptor);
    
      }
   
      
      
      /** returns new instance of generated class
       * @param loader loader used to define class
       * @param interfaces interfaces to implement
       * @param interceptor Interceptor
       * @return proxy object
       */      
      public static Object newProxyInstance(ClassLoader loader, Class[] interfaces, 
                                          MethodInterceptor interceptor) {
        return Enhancer.enhance(null, interfaces, interceptor, loader);
    }
   
      /** returns new instance of generated class
       * @param cls Class to extend
       * @param interfaces interfaces to implement
       * @param interceptor Interceptor
       * @return proxy object
       */      
      public static Object newProxyInstance( Class cls, Class[] interfaces,
                                             MethodInterceptor interceptor) {
        return Enhancer.enhance(cls, interfaces, interceptor, cls.getClassLoader());
    }
  
     /** returns new instance of generated class
       * @param cls Class to extend
       * @param interfaces interfaces to implement
       * @param interceptor Interceptor
       * @param proxied proxied object
       * @return proxy object
       */      
      public static Object newProxyInstance(Class cls, Class[] interfaces, 
                              MethodInterceptor interceptor, Object proxied) {
        return Enhancer.enhance( proxied, cls, interfaces, interceptor,
                                 proxied.getClass().getClassLoader(), (java.lang.reflect.Method)null);
    }
  
      
      /** implements hasCode and equals methods
       * @param keyInterface key Factory interface
       * @return instance of generated Class
       */      
    public static KeyFactory makeFactory(Class keyInterface) {
      return KeyFactory.makeFactory( keyInterface,
                                     keyInterface.getClass().getClassLoader());
    }  
      
      
    
    /** Generates MethodClosure implementation
     * @param delegate instance used for delegation
     * @param methodName method name used for delegation
     * @param iface sole method interface to implement
     * @return instance of generated Class
     */    
    public static MethodClosure closure(Object delegate, String methodName, Class iface) {
        return MethodClosure.generate(delegate, methodName,
                               iface, delegate.getClass().getClassLoader());
    }
    
    
  /**
     * Combines an array of JavaBeans into a single "super" bean.
     * Calls to the super bean will delegate to the underlying beans.
     * In the case of a property name conflicts, the first bean in the list
     * that has the troublesome property will be chosen as the delegate.
     * @param beans the list of beans to delegate to
     * @param loader The ClassLoader to use. If null uses the one that loaded this class.
     * @return the dynamically created bean
     */
    public static Object makeSuperBean(Object[] beans, ClassLoader loader) {
        return Delegator.makeSuperBean(beans, loader);
    }

  
    /**
     * Returns an object that implements all of the specified
     * interfaces. For each interface, all methods are delegated to the
     * respective object in the delegates argument array.
     * @param interfaces the array of interfaces to implement
     * @param delegates The array of delegates. Must be the same length
     * as the interface array, and each delegates must implements the
     * corresponding interface.
     * @param loader The ClassLoader to use. If null uses the one that
     * loaded this class.
     * @return the dynamically created object
     */
    public static Object makeDelegator(Class[] interfaces, Object[] delegates,
                                      ClassLoader loader) {
        return Delegator.makeDelegator(interfaces, delegates, loader );
    }
    
    
}
