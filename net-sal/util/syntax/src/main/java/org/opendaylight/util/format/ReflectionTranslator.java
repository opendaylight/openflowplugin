/*
 * (c) Copyright 2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.format;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.StringTokenizer;

/**
 * Formats message strings by substituting results of parameter-less method
 * invocations for the specified string.
 * 
 * @author Thomas Vachuska
 */
public class ReflectionTranslator extends InvalidTokenTranslator {

    private static final Class<?>[] NO_PARMS = new Class<?>[] {};

    private static final Object[] NO_ARGS = new Object[] {};

    /** Object whose methods the reflection translator invokes. */
    private Object object = null;

    /**
     * Constructs a self-reflecting reflection translator, i.e. one that uses
     * itself as the object on which it will use reflexive translation.
     */
    public ReflectionTranslator() {
        this.object = this;
    }

    /**
     * Constructs a reflection translator that will operate on the specified
     * object.
     * 
     * @param object context object for reflection-based translation
     */
    public ReflectionTranslator(Object object) {
        this.object = object;
    }

    /**
     * Constructs a reflection translator that will operate on the specified
     * object and will treat illegal tokens according to the given behaviour.
     * 
     * @param object context object for reflection-based translation
     * @param behaviour behaviour for dealing with invalid values
     */
    public ReflectionTranslator(Object object, int behaviour) {
        super(behaviour);
        this.object = object;
    }

    /**
     * Returns the object whose methods the reflection translator invokes.
     * 
     * @return current context object
     */
    public Object getObject() {
        return object;
    }

    /**
     * Sets the object whose methods the reflection translator will invoke.
     * 
     * @param object new context object
     */
    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * Translates the given token with the value of method invocation, where
     * the name of the method is the same as the value of the token and the
     * object on which it is invoked is the object currently set on this
     * translator.
     */
    @Override
    public String translate(String token) {
        Object o = object;
        StringTokenizer st = new StringTokenizer(token, ".");
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            try {
                o = getMethod(name, o).invoke(o, NO_ARGS);
            } catch (RuntimeException e) {
                throw e;  // Let the run-time errors go by
            } catch (Exception e) {
                return translateInvalidToken(token);
            }
        }
        return o != null ? o.toString() : translateInvalidToken(token);
    }

    /**
     * Returns the named method declared by the class, or any superclasses, of
     * the current object.
     * @param name method name to fetch on the given object
     * @param o object whose named method should be retrieved
     * @return method or null if one not found
     */
    protected Method getMethod(String name, Object o) {
        Class<?> scanClass = o.getClass();
        while (scanClass != Object.class) {
            try {
                // Fetch the method and make sure we can invoke it, just in
                // case, before returning it.
                final Method m = scanClass.getMethod(name, NO_PARMS);
                return AccessController.doPrivileged(new AccessibleMethod(m));
            } catch (RuntimeException e) {
                throw e;  // Let the run-time errors go by
            } catch (Exception e) {
                scanClass = scanClass.getSuperclass();
            }
        }
        return null;
    }
    
    /**
     * Utility for making a method accessible within a privileged action.
     */
    private static class AccessibleMethod implements PrivilegedAction<Method> {
        
        private Method method;
        
        AccessibleMethod(Method m) {
            this.method = m;
        }

        @Override
        public Method run() {
            method.setAccessible(true);
            return method;
        }

    }

}
