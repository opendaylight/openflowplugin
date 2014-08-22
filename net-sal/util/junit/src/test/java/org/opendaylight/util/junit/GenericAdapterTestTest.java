/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;


/**
 * Unit tests for {@link org.opendaylight.util.junit.GenericAdapterTest}.
 *
 * @author Shaun Wackerly
 */
public class GenericAdapterTestTest {

    /**
     * Tests all that all good return types are correctly handled.
     * 
     * @throws Exception if test problems occur
     */
    @Test 
    public void testMethodReturns() throws Exception {
        class ReturnAdapter {
            public ReturnAdapter() { }
            @SuppressWarnings("unused")
            public void voidReturn() { };
            @SuppressWarnings("unused")
            public Object nullReturn() { return null; };
            @SuppressWarnings("unused")
            public Object[] arrayReturn() { return null; };
            @SuppressWarnings("unused")
            public int zeroReturn() { return 0; };
            @SuppressWarnings("unused")
            public boolean booleanReturn() { return false; };
        }
        class ReturnAdapterTest extends GenericAdapterTest<ReturnAdapter> {
            @Override
            protected ReturnAdapter instance() {
                return new ReturnAdapter();
            }
        }
        
        ReturnAdapterTest rat = new ReturnAdapterTest();
        rat.testAllMethods();
    }

    /**
     * Tests all that all primitive return types are correctly handled.
     * 
     * @throws Exception if test problems occur
     */
    @Test 
    public void testPrimitiveReturns() throws Exception {
        class PrimitiveReturnAdapter {
            public PrimitiveReturnAdapter() { }
            @SuppressWarnings("unused")
            public int zeroIntReturn() { return 0; };
            @SuppressWarnings("unused")
            public long zeroLongReturn() { return 0; };
            @SuppressWarnings("unused")
            public byte zeroByteReturn() { return 0; };
            @SuppressWarnings("unused")
            public short zeroShortReturn() { return 0; };
            @SuppressWarnings("unused")
            public float zeroFloatReturn() { return 0; };
            @SuppressWarnings("unused")
            public double zeroDoubleReturn() { return 0; };
            @SuppressWarnings("unused")
            public char zeroCharReturn() { return 0; };
        }
        class ReturnAdapterTest extends GenericAdapterTest<PrimitiveReturnAdapter> {
            @Override
            protected PrimitiveReturnAdapter instance() {
                return new PrimitiveReturnAdapter();
            }
        }
        
        ReturnAdapterTest rat = new ReturnAdapterTest();
        rat.testAllMethods();
    }

    /**
     * Test that all types of method parameters are correctly handled.
     * 
     * @throws Exception if test problems occur
     */
    @Test
    public void testMethodArguments() throws Exception {
        class ArgumentAdapter {
            @SuppressWarnings("unused")
            public void voidArgs() { };
            @SuppressWarnings("unused")
            public void primitiveArgs(byte by, short s, int i, long l, float f,
                                      double d, boolean bo, char c) { };
            @SuppressWarnings("unused")
            public void objectArgs(Object o, Object[] oa) { };
            @SuppressWarnings("unused")
            public void varArgs(Object... o) { };
        }
        class ArgumentAdapterTest extends GenericAdapterTest<ArgumentAdapter> {
            @Override
            protected ArgumentAdapter instance() {
                return new ArgumentAdapter();
            }
        }
        
        ArgumentAdapterTest aat = new ArgumentAdapterTest();
        aat.testAllMethods();
    }

    /**
     * Tests all that all levels of method accessibility are correctly handled.
     * 
     * @throws Exception if test problems occur
     */
    @Test 
    public void testMethodAccessibility() throws Exception {
        class AccessibilityAdapter {
            @SuppressWarnings("unused")
            private int privateMethod() { return 0; };
            @SuppressWarnings("unused")
            protected void protectedMethod() { };
            @SuppressWarnings("unused")
            Object packagePrivateMethod() { return null; };
            @SuppressWarnings("unused")
            public final void finalMethod() { };
        }
        class AccessibilityAdapterTest extends GenericAdapterTest<AccessibilityAdapter> {
            @Override
            protected AccessibilityAdapter instance() {
                return new AccessibilityAdapter();
            }
        }
        
        AccessibilityAdapterTest aat = new AccessibilityAdapterTest();
        aat.testAllMethods();
    }

    /**
     * Test that a non-null return will cause a unit test failure.
     * 
     * @throws Exception if test problems occur
     */
    @Test (expected = AssertionError.class)
    public void testNonNullReturn() throws Exception {
        class NonNullAdapter {
            @SuppressWarnings("unused")
            public Object nonNullReturn() { return new Object(); };
        }
        class NonNullAdapterTest extends GenericAdapterTest<NonNullAdapter> {
            @Override
            protected NonNullAdapter instance() {
                return new NonNullAdapter();
            }
        }
        
        NonNullAdapterTest nnat = new NonNullAdapterTest();
        nnat.testAllMethods();
    }

    /**
     * Test that a non-zero return will cause a unit test failure.
     * Test a variable argument method to assure that such methods are
     * tested properly.
     * 
     * @throws Exception if test problems occur
     */
    @Test (expected = AssertionError.class)
    public void testNonZeroReturn() throws Exception {
        class NonZeroAdapter {
            @SuppressWarnings("unused")
            public Object nonZeroReturn(Object... o) { return new Object(); };
        }
        class NonZeroAdapterTest extends GenericAdapterTest<NonZeroAdapter> {
            @Override
            protected NonZeroAdapter instance() {
                return new NonZeroAdapter();
            }
        }
        
        NonZeroAdapterTest nzat = new NonZeroAdapterTest();
        nzat.testAllMethods();
    }

    /**
     * Test that a thrown exception will cause a unit test failure.
     * 
     * @throws Exception if test problems occur
     */
    @Test
    public void testThrowException() throws Exception {
        final String msg = new Double(Math.random()).toString();
        class ThrowAdapter {
            @SuppressWarnings("unused")
            public Object throwMethod() throws Exception { throw new Exception(msg); };
        }
        class ThrowAdapterTest extends GenericAdapterTest<ThrowAdapter> {
            @Override
            protected ThrowAdapter instance() {
                return new ThrowAdapter();
            }
        }
        
        ThrowAdapterTest tat = new ThrowAdapterTest();
        try {
            tat.testAllMethods();
        } catch (InvocationTargetException ite) {
            assertEquals(Exception.class, ite.getCause().getClass());
            assertEquals(msg, ite.getCause().getMessage());
        }
    }
    
}
