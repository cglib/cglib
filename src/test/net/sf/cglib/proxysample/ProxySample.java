/*
 * Copyright 2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            throw new Error(e.getMessage());
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
