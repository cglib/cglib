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

/**
 * @version $Id: MethodConstants.java,v 1.10 2003/01/31 01:18:50 herbyderby Exp $
 */
abstract class MethodConstants {
    public static final Method EQUALS =
      ReflectUtils.findMethod("Object.equals(Object)");
    public static final Method GET_DECLARED_METHOD =
      ReflectUtils.findMethod("Class.getDeclaredMethod(String, Class[])");
    public static final Method GET_DECLARED_CONSTRUCTOR = 
      ReflectUtils.findMethod("Class.getDeclaredConstructor(Class[])");
    public static final Method HASH_CODE =
      ReflectUtils.findMethod("Object.hashCode()");
    public static final Method FLOAT_TO_INT_BITS =
      ReflectUtils.findMethod("Float.floatToIntBits(float)");
    public static final Method DOUBLE_TO_LONG_BITS =
      ReflectUtils.findMethod("Double.doubleToLongBits(double)");
    public static final Method FOR_NAME =
      ReflectUtils.findMethod("Class.forName(String)");
    public static final Method THROWABLE_GET_MESSAGE =
      ReflectUtils.findMethod("Throwable.getMessage()");
    public static final Method DEFINE_CLASS =
      ReflectUtils.findMethod("ClassLoader.defineClass( byte[], int, int)");
    public static final Method BOOLEAN_VALUE =
      ReflectUtils.findMethod("Boolean.booleanValue()");
    public static final Method CHAR_VALUE =
      ReflectUtils.findMethod("Character.charValue()");
    public static final Method LONG_VALUE =
      ReflectUtils.findMethod("Number.longValue()");
    public static final Method DOUBLE_VALUE =
      ReflectUtils.findMethod("Number.doubleValue()");
    public static final Method FLOAT_VALUE =
      ReflectUtils.findMethod("Number.floatValue()");
    public static final Method INT_VALUE =
      ReflectUtils.findMethod("Number.intValue()");
    public static final Method MAP_PUT =
      ReflectUtils.findMethod("java.util.Map.put(Object,Object)");
    public static final Method MAP_GET =
      ReflectUtils.findMethod("java.util.Map.get(Object)");
}
