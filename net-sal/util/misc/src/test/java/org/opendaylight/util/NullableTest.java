/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import junit.framework.Assert;

import org.junit.Test;

import org.opendaylight.util.junit.EqualityTester;
import org.opendaylight.util.junit.SerializabilityTester;
import org.opendaylight.util.junit.SerializabilityTester.SemanticCompatibilityVerifier;

/**
 * @author Fabiel Zuniga
 */
public class NullableTest {

    @Test
    public void testNullValue() {
        Nullable<Integer> nullable = Nullable.nullValue();
        Assert.assertNull(nullable.getValue());
    }

    @Test
    public void testValueOf() {
        Nullable<Integer> nullable = Nullable.valueOf(Integer.valueOf(1));
        Assert.assertEquals(1, nullable.getValue().intValue());
    }

    @Test
    public void testEqualsAndHashCode() {
        Nullable<Integer> baseObjToTest = Nullable.valueOf(Integer.valueOf(1));
        Nullable<Integer> equalsToBase1 = Nullable.valueOf(Integer.valueOf(1));
        Nullable<Integer> equalsToBase2 = Nullable.valueOf(Integer.valueOf(1));
        Nullable<Integer> unequalToBase1 = Nullable.valueOf(Integer.valueOf(2));
        Nullable<Integer> unequalToBase2 = Nullable.nullValue();

        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase2);

        baseObjToTest = Nullable.nullValue();
        equalsToBase1 = Nullable.nullValue();
        equalsToBase2 = Nullable.nullValue();
        unequalToBase1 = Nullable.valueOf(Integer.valueOf(2));

        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
    }

    @Test
    public void testSe() {
        SemanticCompatibilityVerifier<Nullable<Integer>> semanticCompatibilityVerifier = new SemanticCompatibilityVerifier<Nullable<Integer>>() {
            @Override
            public void assertSemanticCompatibility(Nullable<Integer> original, Nullable<Integer> replica) {
                Assert.assertEquals(original.getValue(), replica.getValue());
            }
        };

        SerializabilityTester.testSerialization(Nullable.valueOf(Integer.valueOf(1)), semanticCompatibilityVerifier);
    }

    @Test
    public void testToString() {
        Assert.assertFalse(Nullable.valueOf(Integer.valueOf(1)).toString().isEmpty());
        Assert.assertFalse(Nullable.nullValue().toString().isEmpty());
    }
}
