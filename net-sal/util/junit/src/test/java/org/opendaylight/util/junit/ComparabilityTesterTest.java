/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import java.util.Comparator;

import org.junit.Assert;
import org.junit.Test;

import org.opendaylight.util.junit.ThrowableTester.Instruction;
import org.opendaylight.util.junit.ThrowableTester.Validator;

/**
 * Test for {@link org.opendaylight.util.junit.ComparabilityTester}
 * 
 * @author Fabiel Zuniga
 */
public class ComparabilityTesterTest {

    @Test
    public void testTestComparison() {
        Integer first = Integer.valueOf(0);
        Integer equallyInOrderToFirst = Integer.valueOf(0);
        Integer second = Integer.valueOf(5);
        Integer third = Integer.valueOf(6);

        ComparabilityTester.testComparison(first, equallyInOrderToFirst,
                                           second, third);
    }

    @Test
    public void testEqualToFail() {
        final Object first = new Object();
        final Object equallyInOrderToFirst = new Object();
        final Object second = new Object();
        final Object third = new Object();

        final Comparator<Object> comparator = new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                if (o1 == first && o2 == equallyInOrderToFirst) {
                    return -1; // Invalid
                } else if (o1 == first && o2 == second) {
                    return -1;
                } else if (o1 == second && o2 == first) {
                    return 1;
                } else if (o1 == second && o2 == third) {
                    return -1;
                } else if (o1 == first && o2 == third) {
                    return -1;
                } else if (o1 == third && o2 == second) {
                    return 1;
                } else if (o1 == second && o2 == first) {
                    return 1;
                } else if (o1 == third && o2 == first) {
                    return 1;
                }

                throw new RuntimeException("case not considered");
            }
        };

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "'Equal to' comparison broken";
                Assert.assertEquals(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                ComparabilityTester.testComparison(first,
                                                   equallyInOrderToFirst,
                                                   second, third, comparator);
            }
        }, errorValidator);
    }

    @Test
    public void testLessThanFail() {
        final Object first = new Object();
        final Object equallyInOrderToFirst = new Object();
        final Object second = new Object();
        final Object third = new Object();

        final Comparator<Object> comparator = new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                if (o1 == first && o2 == equallyInOrderToFirst) {
                    return 0;
                } else if (o1 == first && o2 == second) {
                    return 0; // Invalid
                } else if (o1 == second && o2 == first) {
                    return 1;
                } else if (o1 == second && o2 == third) {
                    return -1;
                } else if (o1 == first && o2 == third) {
                    return -1;
                } else if (o1 == third && o2 == second) {
                    return 1;
                } else if (o1 == second && o2 == first) {
                    return 1;
                } else if (o1 == third && o2 == first) {
                    return 1;
                }

                throw new RuntimeException("case not considered");
            }
        };

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "'Less than' comparison broken";
                Assert.assertEquals(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                ComparabilityTester.testComparison(first,
                                                   equallyInOrderToFirst,
                                                   second, third, comparator);
            }
        }, errorValidator);
    }

    @Test
    public void testGreaterThanFail() {
        final Object first = new Object();
        final Object equallyInOrderToFirst = new Object();
        final Object second = new Object();
        final Object third = new Object();

        final Comparator<Object> comparator = new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                if (o1 == first && o2 == equallyInOrderToFirst) {
                    return 0;
                } else if (o1 == first && o2 == second) {
                    return -1;
                } else if (o1 == second && o2 == first) {
                    return 0; // Invalid
                } else if (o1 == second && o2 == third) {
                    return -1;
                } else if (o1 == first && o2 == third) {
                    return -1;
                } else if (o1 == third && o2 == second) {
                    return 1;
                } else if (o1 == second && o2 == first) {
                    return 1;
                } else if (o1 == third && o2 == first) {
                    return 1;
                }

                throw new RuntimeException("case not considered");
            }
        };

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "'Greater than' comparison broken";
                Assert.assertEquals(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                ComparabilityTester.testComparison(first,
                                                   equallyInOrderToFirst,
                                                   second, third, comparator);
            }
        }, errorValidator);
    }

    @Test
    public void testTransitiveLessThanFail() {
        final Object first = new Object();
        final Object equallyInOrderToFirst = new Object();
        final Object second = new Object();
        final Object third = new Object();

        final Comparator<Object> comparator = new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                if (o1 == first && o2 == equallyInOrderToFirst) {
                    return 0;
                } else if (o1 == first && o2 == second) {
                    return -1;
                } else if (o1 == second && o2 == first) {
                    return 1;
                } else if (o1 == second && o2 == third) {
                    return -1;
                } else if (o1 == first && o2 == third) {
                    return 0; // Invalid
                } else if (o1 == third && o2 == second) {
                    return 1;
                } else if (o1 == second && o2 == first) {
                    return 1;
                } else if (o1 == third && o2 == first) {
                    return 1;
                }

                throw new RuntimeException("case not considered");
            }
        };

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "'Less than' transitive comparison broken";
                Assert.assertEquals(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                ComparabilityTester.testComparison(first,
                                                   equallyInOrderToFirst,
                                                   second, third, comparator);
            }
        }, errorValidator);
    }

    @Test
    public void testTransitiveGreaterThanFail() {
        final Object first = new Object();
        final Object equallyInOrderToFirst = new Object();
        final Object second = new Object();
        final Object third = new Object();

        final Comparator<Object> comparator = new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                if (o1 == first && o2 == equallyInOrderToFirst) {
                    return 0;
                } else if (o1 == first && o2 == second) {
                    return -1;
                } else if (o1 == second && o2 == first) {
                    return 1;
                } else if (o1 == second && o2 == third) {
                    return -1;
                } else if (o1 == first && o2 == third) {
                    return -1;
                } else if (o1 == third && o2 == second) {
                    return 1;
                } else if (o1 == second && o2 == first) {
                    return 1;
                } else if (o1 == third && o2 == first) {
                    return 0; // Invalid
                }

                throw new RuntimeException("case not considered");
            }
        };

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "'Greater than' transitive comparison broken";
                Assert.assertEquals(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                ComparabilityTester.testComparison(first,
                                                   equallyInOrderToFirst,
                                                   second, third, comparator);
            }
        }, errorValidator);
    }
}
