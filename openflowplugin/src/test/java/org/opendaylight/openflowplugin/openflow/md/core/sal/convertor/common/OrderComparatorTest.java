/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.Ordered;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * To test OrderComparator
 */
public class OrderComparatorTest {

    @Test
    public void testBothObjectsNull() {
        try {
            OrderComparator.<MockOrderedObject>build().compare(null, null);
            org.junit.Assert.fail("Passing null to OrderCompartor should raise " +
                "a NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    @Test
    public void testEitherObjectNull() {
        try {
            MockOrderedObject obj1 = new MockOrderedObject();
            obj1.setOrder(0);
            MockOrderedObject obj2 = null;

            OrderComparator.<MockOrderedObject>build().compare(obj1, obj2);
            org.junit.Assert.fail("Passing null to OrderCompartor should raise " +
                "a NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        try {
            MockOrderedObject obj1 = null;
            MockOrderedObject obj2 = new MockOrderedObject();
            obj2.setOrder(1);

            OrderComparator.<MockOrderedObject>build().compare(obj1, obj2);
            org.junit.Assert.fail("Passing null to OrderCompartor should raise " +
                "a NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    @Test
    public void testCompare() {
        MockOrderedObject obj1 = new MockOrderedObject();
        obj1.setOrder(0);
        MockOrderedObject obj2 = new MockOrderedObject();
        obj2.setOrder(1);

        Assert.assertEquals(-1,
            OrderComparator.<MockOrderedObject>build().compare(obj1, obj2));

        obj1 = new MockOrderedObject();
        obj1.setOrder(1);
        obj2 = new MockOrderedObject();
        obj2.setOrder(0);

        Assert.assertEquals(1,
            OrderComparator.<MockOrderedObject>build().compare(obj1, obj2));

        obj1 = new MockOrderedObject();
        obj1.setOrder(1);
        obj2 = new MockOrderedObject();
        obj2.setOrder(1);

        Assert.assertEquals(0,
            OrderComparator.<MockOrderedObject>build().compare(obj1, obj2));
    }


    public class MockOrderedObject implements Ordered {
        private Integer order;

        public void setOrder(Integer order) {
            this.order = order;
        }

        @Override
        public Integer getOrder() {
            return order;
        }

        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return Ordered.class;
        }
    }

}
