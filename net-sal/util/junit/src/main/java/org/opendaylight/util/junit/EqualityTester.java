/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import junit.framework.Assert;

/**
 * Tester to test objects which override {@link Object#equals(Object)} and
 * {@link Object#hashCode()}.
 * 
 * @author Fabiel Zuniga
 */
public final class EqualityTester {

    private static final DifferetType DIFFERENT_TYPE = new DifferetType();

    private EqualityTester() {

    }

    /**
     * Tests {@link Object#equals(Object)} and {@link Object#hashCode()}
     * methods partially testing consistency property: Fields that are not
     * part of the {@link Object#equals(Object)} are not modified - no
     * {@link Exerciser} is used.
     * 
     * @param <T> type of the object to test
     * @param base base object
     * @param equalsToBase1 an object expected to be equal to the base
     * @param equalsToBase2 an object expected to be equal to the base
     * @param unequalToBase a set of objects expected not to be equal to the
     *        base
     */
    @SafeVarargs
    public static <T> void testEqualsAndHashCode(T base, T equalsToBase1,
                                                 T equalsToBase2,
                                                 T... unequalToBase) {
        testEqualsAndHashCode(base, equalsToBase1, equalsToBase2, null,
                              unequalToBase);
    }

    /**
     * Tests {@link Object#equals(Object)} and {@link Object#hashCode()}
     * methods using the given {@code exerciser} to test consistency.
     * 
     * @param <T> type of the object to test
     * @param base base object
     * @param equalsToBase1 an object expected to be equal to the base
     * @param equalsToBase2 an object expected to be equal to the base
     * @param exerciser exerciser used to test consistency
     * @param unequalToBase a set of objects expected not to be equal to the
     *        base
     */
    @SafeVarargs
    public static <T> void testEqualsAndHashCode(T base, T equalsToBase1,
                                                 T equalsToBase2,
                                                 Exerciser<T> exerciser,
                                                 T... unequalToBase) {

        String testedObjectClassName = base.getClass().getCanonicalName();

        // Equals contract

        // Reflexive
        Assert.assertTrue("Reflexive property broken for "
                + testedObjectClassName, base.equals(base));

        // Symmetric
        Assert.assertTrue("Symmetric property broken for "
                + testedObjectClassName, base.equals(equalsToBase1)
                && equalsToBase1.equals(base));

        // Transitive
        Assert.assertTrue("Transitive property broken for "
                                  + testedObjectClassName,
                          base.equals(equalsToBase1)
                                  && equalsToBase1.equals(equalsToBase2)
                                  && base.equals(equalsToBase2));

        // Null reference
        Assert.assertFalse("Null reference property broken for "
                + testedObjectClassName, base.equals(null));

        // Different type parameter
        Assert.assertFalse("Different type parameter consideration broken for "
                + testedObjectClassName, base.equals(DIFFERENT_TYPE));

        // Inequality test
        if (unequalToBase != null) {
            for (T unequal : unequalToBase) {
                Assert.assertFalse("Inequality test broken for "
                        + testedObjectClassName, base.equals(unequal));
                Assert.assertFalse("Inequality test broken for "
                        + testedObjectClassName, unequal.equals(base));
            }
        }

        // Hash code
        Assert.assertEquals("Hashcode broken for " + testedObjectClassName,
                            base.hashCode(), equalsToBase1.hashCode());
        Assert.assertEquals("Hashcode broken for " + testedObjectClassName,
                            base.hashCode(), equalsToBase2.hashCode());

        // Consistent property

        if (exerciser != null) {
            exerciser.exercise(base);
        }

        Assert.assertTrue("Consistent property broken for "
                + testedObjectClassName, base.equals(equalsToBase1));
        Assert.assertFalse("Consistent property broken for "
                + testedObjectClassName, base.equals(unequalToBase));
        Assert.assertEquals("Hashcode consistent property broken for "
                                    + testedObjectClassName, base.hashCode(),
                            equalsToBase1.hashCode());
    }

    /**
     * Exerciser used when testing consistent property in equals method.
     * 
     * @param <T> type of the object to exercise
     */
    public static interface Exerciser<T> {

        /**
         * Exercises the object without modifying fields considered on equals
         * method. This method is used to test that
         * {@link Object#equals(Object)} is consistent.
         * 
         * @param obj Object to exercise
         */
        public void exercise(T obj);
    }

    private static class DifferetType {

    }
}
