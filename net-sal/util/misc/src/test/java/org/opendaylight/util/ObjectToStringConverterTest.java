/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import junit.framework.Assert;

import org.junit.Test;
import org.opendaylight.util.ObjectToStringConverter;
import org.opendaylight.util.Property;

/**
 * {@link org.opendaylight.util.ObjectToStringConverter} tests.
 * 
 * @author Fabiel Zuniga
 */
public class ObjectToStringConverterTest {

    @Test
    public void testToString() {
        ConcreteObject concreteObject = new ConcreteObject(1, true);
        String expected = "ConcreteObject[property1=1, property2=true]";
        Assert.assertEquals(expected, concreteObject.toString());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testToStringInvalidSubject() {
        ObjectToStringConverter.toString(null, Property.valueOf("property", "propertyValue"));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testToStringInvalidProperties() {
        ObjectToStringConverter.toString(new Object());
    }

    private static class ConcreteObject {

        private int property1;
        private boolean property2;

        public ConcreteObject(int property1, boolean property2) {
            this.property1 = property1;
            this.property2 = property2;
        }

        @SuppressWarnings("unchecked")
        @Override
        public String toString() {
            return ObjectToStringConverter.toString(
                    this, 
                    Property.valueOf("property1", Integer.valueOf(this.property1)),
                    Property.valueOf("property2", Boolean.valueOf(this.property2))
            ); 
        }
    }
}
