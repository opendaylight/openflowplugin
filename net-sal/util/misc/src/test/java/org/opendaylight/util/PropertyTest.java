/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import org.opendaylight.util.junit.EqualityTester;
import org.opendaylight.util.junit.SerializabilityTester;

/**
 * {@link org.opendaylight.util.Property} tests.
 * 
 * @author Fabiel Zuniga
 */
public class PropertyTest {

    @Test
    public void testConstruction() {
        Object identity = EasyMock.createMock(Object.class);
        Object value = EasyMock.createMock(Object.class);

        Property<Object, Object> property = Property.valueOf(identity, value);

        Assert.assertSame(identity, property.getIdentity());
        Assert.assertSame(value, property.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidConstruction() {
        Object invalidIdentity = null;
        Object validValue = null;
        Property.valueOf(invalidIdentity, validValue);
    }

    @Test
    public void testEqualsAndHashCode() {
        Object identity = new Object();

        Property<Object, Object> baseObjToTest = Property.valueOf(identity, new Object());
        Property<Object, Object> equalsToBase1 = Property.valueOf(identity, new Object());
        Property<Object, Object> equalsToBase2 = Property.valueOf(identity, new Object());
        Property<Object, Object> unequalToBase = Property.valueOf(new Object(), new Object());

        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase);
    }

    @Test
    public void testSerialization() {
        SerializabilityTester.testSerialization(Property.valueOf("subject", "subject"));
    }

    @Test
    public void testToString() {
        Property<String, String> property = Property.valueOf("subject", "subject");
        Assert.assertFalse(property.toString().isEmpty());
    }
}
