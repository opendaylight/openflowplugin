/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import junit.framework.Assert;

/**
 * Throwable tester.
 * <p>
 * Note: Throwable isn't generally caught. Throwable is the superclass to
 * Exception and Error. Errors are generally things which a normal application
 * wouldn't and shouldn't catch, so just use Exception unless you have a
 * specific reason to use Throwable. In this case ThrowableTester checks
 * whether an expected Throwable is actually thrown.
 * <p>
 * Note that JUnit already offers a way to assert expected exceptions, however
 * for complex setups the exception may come from an unexpected source hiding
 * a problem. Example:
 * 
 * <pre>
 * // Good usage of the JUnit annotation 
 * {@literal @}Test(expected = NullPointerException.class)
 * public void test() {
 *     ClassUnderTest testable = ...;
 *     testable.methodExpectingToThrowException();
 * }
 * 
 * // Dangerous usage of the JUnit annotation
 * {@literal @}Test(expected = NullPointerException.class)
 * public void test() {
 *     setUpOperation_1;
 *     ...                 // If any of the setup operations throw a NullPointerException the test will pass hiding a problem
 *     setUpOperation_n;
 *     ClassUnderTest testable = ...;
 *     testable.methodExpectingToThrowException();
 * }
 * 
 * // Alternative using ThrowableTester
 * {@literal @}Test
 * public void test() {
 *     setUpOperation_1;
 *     ...                 // If any of the setup operations throw a NullPointerException the test will fail uncovering a problem
 *     setUpOperation_n;
 *     final ClassUnderTest testable = ...;
 *         
 *     ThrowableTester.testThrows(NullPointerException.class, new Instruction(){
 *         {@literal @}Override
 *         public void execute() throws Throwable {
 *             testable.methodExpectingToThrowException();
 *         }
 *     });
 * }
 * </pre>
 */
public final class ThrowableTester {

    private ThrowableTester() {

    }

    /**
     * Asserts that an exception is of (or a subclass of) the given expected
     * type.
     * 
     * @param expected expected {@link Throwable} type
     * @param actual actual {@link Throwable}
     */
    public static <T extends Throwable> void assertAnyThrowableType(Class<T> expected,
                                                                    Throwable actual) {
        Assert.assertTrue("Invalid throwable, expected any <"
                                  + expected.getName() + "> but was <"
                                  + actual.getClass().getName() + ">",
                          expected.isInstance(actual));
    }

    /**
     * Asserts that an exception is exactly of the given expected type.
     * 
     * @param expected expected {@link Throwable} type
     * @param actual actual {@link Throwable}
     */
    public static <T extends Throwable> void assertThrowableType(Class<T> expected,
                                                                 Throwable actual) {
        Assert.assertEquals("Invalid throwable,", expected, actual.getClass());
    }

    /**
     * Executes an instruction verifying a {@link Throwable} of (or a subclass
     * of) the expected type is actually thrown.
     * 
     * @param expectedThrowable expected {@link Throwable}
     * @param instruction instruction to execute
     */
    public static <T extends Throwable> void testThrowsAny(Class<T> expectedThrowable,
                                                           Instruction instruction) {
        testThrowsAny(expectedThrowable, instruction, null);
    }

    /**
     * Executes an instruction verifying a {@link Throwable} of (or a subclass
     * of) the expected type is actually thrown.
     * 
     * @param expectedThrowable expected {@link Throwable}
     * @param instruction instruction to execute
     * @param throwableValidator validator is used to do additional inspection
     *        to the error
     */
    public static <T extends Throwable> void testThrowsAny(Class<T> expectedThrowable,
                                                           Instruction instruction,
                                                           Validator<T> throwableValidator) {
        boolean fail = false;
        try {
            instruction.execute();
            fail = true;
        } catch (Throwable e) {
            assertAnyThrowableType(expectedThrowable, e);
            if (throwableValidator != null) {
                @SuppressWarnings("unchecked")
                T throwable = (T) e;
                throwableValidator.assertThrowable(throwable);
            }
        }

        if (fail) {
            Assert.fail("Expected any <" + expectedThrowable.getName()
                    + "> but nothing was thrown");
        }
    }

    /**
     * Executes an instruction verifying a {@link Throwable} of the expected
     * type is actually thrown.
     * 
     * @param expectedThrowable expected {@link Throwable}
     * @param instruction instruction to execute
     */
    public static <T extends Throwable> void testThrows(Class<T> expectedThrowable,
                                                        Instruction instruction) {
        testThrows(expectedThrowable, instruction, null);
    }

    /**
     * Executes an instruction verifying a {@link Throwable} of the expected
     * type is actually thrown.
     * 
     * @param expectedThrowable expected {@link Throwable}
     * @param instruction instruction to execute
     * @param throwableValidator validator is used to do additional inspection
     *        to the error
     */
    public static <T extends Throwable> void testThrows(Class<T> expectedThrowable,
                                                        Instruction instruction,
                                                        Validator<T> throwableValidator) {
        boolean fail = false;
        try {
            instruction.execute();
            fail = true;
        } catch (Throwable e) {
            assertThrowableType(expectedThrowable, e);
            if (throwableValidator != null) {
                @SuppressWarnings("unchecked")
                T throwable = (T) e;
                throwableValidator.assertThrowable(throwable);
            }
        }

        if (fail) {
            Assert.fail("Expected <" + expectedThrowable.getName()
                    + "> but nothing was thrown");
        }
    }

    /**
     * Instruction.
     */
    public static interface Instruction {

        /**
         * Executes instructions.
         * 
         * @throws Throwable if errors occur during execution
         */
        public void execute() throws Throwable;
    }

    /**
     * Throwable validator. This validator is used to do additional inspection
     * to the error. For example if the error contains data, such data could
     * be asserted to be valid.
     * 
     * @param <E> type of the
     */
    public static interface Validator<E extends Throwable> {

        /**
         * Assert the error is as expected.
         * 
         * @param throwable throwable
         */
        public void assertThrowable(E throwable);
    }
}
