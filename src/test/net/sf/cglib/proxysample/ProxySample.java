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
package net.sf.cglib.proxysample;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.UndeclaredThrowableException;

public final class ProxySample implements ProxySampleInterface_ReturnsObject, ProxySampleInterface_ReturnsBasic {
	
	private InvocationHandler handler = null;

    protected ProxySample(InvocationHandler handler) {
    	this.handler = handler;
    }

    public String getKala(String kalamees) throws Exception {
    	String result =  null;
        try {
        	// invocation is also generated
            result = (String) handler.invoke(this, ProxySampleInterface_ReturnsObject.class.getMethod("getKala", new Class[] {String.class}), new Object[] {kalamees});
        } catch (ClassCastException e) {
        	throw e;
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        } catch (RuntimeException e) {
        	throw e;
        } catch (Exception e) {
        	// generated: catch the exception throwed by interface method and re-throw it
        	throw e;
        } catch (Error e) {
        	throw e;
        } catch (Throwable e) {
        	throw new UndeclaredThrowableException(e);
        }
        return result;
    }

    public int getKala(float kalamees) {
    	Integer result =  null;
        try {
        	// invocation is also generated
            result = (Integer) handler.invoke(this, ProxySampleInterface_ReturnsBasic.class.getMethod("getKala", new Class[] {Float.TYPE}), new Object[] {new Float(kalamees)});
        } catch (ClassCastException e) {
        	throw e;
        } catch (NoSuchMethodException e) {
        	// ignore, the method has to be found, as this class is generated
        } catch (RuntimeException e) {
        	throw e;
        } catch (Error e) {
        	throw e;
        } catch (Throwable e) {
        	throw new UndeclaredThrowableException(e);
        }
        return result.intValue();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String result =  null;
        try {
            // invocation is also generated
            result = (String) handler.invoke(this, Object.class.getMethod("toString", null), null);
        } catch (ClassCastException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            // ignore, the method has to be found, as this class is generated
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
        return result;
    }

}
