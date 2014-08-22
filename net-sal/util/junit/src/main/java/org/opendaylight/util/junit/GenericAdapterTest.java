/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Test;

/**
 * A parameterized adapter test class that will execute all methods of
 * the given adapter class. The test will verify that all methods return zero,
 * null, or void which is the expected behavior of any adapter.
 * <P>
 * This test class should only be used to test base adapters, not adapters
 * that extend other adapters. If an adapter extends another adapter, it
 * is usually replacing the default adapter behavior with its own. This class
 * verifies the default adapter behavior.
 * 
 * @param <A> The class of adapter to test
 * 
 * @author Shaun Wackerly
 */
public abstract class GenericAdapterTest<A> {
    
    // FIXME: Java implementation uses the same value for
    // Modifier.VARARGS (private) and Modifier.TRANSIENT. Until this is
    // fixed, we consider 'transient' to be a valid method modifier so that
    // unit tests with variable argument lists will be checked.
    /** The set of possible method modifiers. */
    private static final int possibleMods = Modifier.methodModifiers() | Modifier.TRANSIENT;

    /**
     * Gets an instance of the adapter to test.
     * 
     * @return An instance of the adapter
     */
    protected abstract A instance();

    /**
     * Gets the default value to use for the given type.
     * 
     * @param type The given type
     * @return The default value for the given type
     */
    private Object defaultValue(Class<?> type) {
        // Primitives must be handled specially, cannot be null.
        if (type.isPrimitive()) {
            if (type == boolean.class)
                return false;
            if (type == char.class)
                return '\u0000';
            if (type == byte.class)
                return (byte)0;
            if (type == short.class)
                return (short)0;
            
            // All other primitives use 0
            return 0;
        }
        
        return null;
    }
    
    /**
     * Tests all methods in the class, using introspection. Default values
     * are given for all primitives and null is given for all objects.
     * This test assures that each method will execute and return either
     * void, zero, or null.
     * 
     * @throws Exception if any problems occur during the test
     */
    @Test
    public void testAllMethods() throws Exception {
        A adapter = instance();

        // Execute each method with default values
        Method[] methods = adapter.getClass().getDeclaredMethods();
        for (Method m : methods) {
            print(m);

            // Skip any methods that have modifiers which cannot be applied
            // to a method. This is a hackish way of not running tests against
            // dynamically-added methods such as are used when measuring
            // code coverage.
            int mods = m.getModifiers();
            if (mods != (possibleMods & mods)) {
                print("Skipping method w/invalid modifiers: actual=0x%x valid=0x%x",
                      mods, possibleMods);
                continue;
            }
            
            // If the method is private/protected make sure we can call it
            m.setAccessible(true);

            // Get a default value for each parameter to the method
            Class<?>[] types = m.getParameterTypes();
            Object[] args = new Object[types.length];
            for (int ndx = 0; ndx < args.length; ndx++)
                args[ndx] = defaultValue(types[ndx]);
            
            // Invoke the adapter's method
            Object o = m.invoke(adapter, args);
            
            // Verify that the return value is either null or zero.
            // All void methods return 'null' from Method.invoke().
            if (o != null) {
                // Default casting is to integer, so we must explicitly cast
                // to other types which aren't implicitly handled.
                Class<?> retType = m.getReturnType();
                if (long.class == retType)
                    assertEquals(AM_NEQ, Long.valueOf(0), o);
                else if (char.class == retType)
                    assertEquals(AM_NEQ, Character.valueOf((char)0), o);
                else if (byte.class == retType)
                    assertEquals(AM_NEQ, Byte.valueOf((byte)0), o);
                else if (short.class == retType)
                    assertEquals(AM_NEQ, Short.valueOf((short)0), o);
                else if (float.class == retType)
                    assertEquals(AM_NEQ, Float.valueOf((float)0), o);
                else if (double.class == retType)
                    assertEquals(AM_NEQ, Double.valueOf((double)0), o);
                else if (boolean.class == retType)
                    assertEquals(AM_NEQ, false, o);
                else
                    assertEquals(AM_NEQ, 0, o);
            }
        }
    }
}
