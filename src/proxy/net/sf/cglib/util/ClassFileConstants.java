/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
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
package net.sf.cglib.util;

import net.sf.cglib.proxy.MethodInterceptor;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
/**
 * private utility
 * @author  baliuka
 * @deprecated ClassFileConstants is internal utility and can be removed in the future
 */
public interface ClassFileConstants extends org.apache.bcel.Constants{

  public  static final String INTERCEPTOR_CLASS_NAME = MethodInterceptor.class.getName();
  public  static final ObjectType BOOLEAN_OBJECT =
    new ObjectType(Boolean.class.getName());
  public   static final ObjectType INTEGER_OBJECT =
    new ObjectType(Integer.class.getName());
  public    static final ObjectType CHARACTER_OBJECT =
    new ObjectType(Character.class.getName());
  public  static final ObjectType BYTE_OBJECT = new ObjectType(Byte.class.getName());
  public  static final ObjectType SHORT_OBJECT = new ObjectType(Short.class.getName());
  public  static final ObjectType LONG_OBJECT = new ObjectType(Long.class.getName());
  public  static final ObjectType DOUBLE_OBJECT = new ObjectType(Double.class.getName());
  public  static final ObjectType FLOAT_OBJECT = new ObjectType(Float.class.getName());
  public  static final ObjectType METHOD_OBJECT =
    new ObjectType(java.lang.reflect.Method.class.getName());
  public  static final ObjectType CLASS_OBJECT = new ObjectType(Class.class.getName());
  public  static final ObjectType NUMBER_OBJECT = new ObjectType(Number.class.getName());
  public  static final String CONSTRUCTOR_NAME = "<init>";
  public  static final String SOURCE_FILE = "<generated>";
  public  static final String FIND_CLASS = "CGLIB$findClass";
     
}
