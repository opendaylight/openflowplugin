/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

import org.opendaylight.util.junit.EqualityTester;
import org.opendaylight.util.junit.SerializabilityTester;
import org.opendaylight.util.junit.ThrowableTester;
import org.opendaylight.util.junit.ThrowableTester.Instruction;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link Id}
 * 
 * @author Fabiel Zuniga
 */
public class IdTest {

    /**
     * 
     */
    @Test
    public void testConstruction() {
        Id<String, String> id = Id.valueOf("id");
        Assert.assertEquals("id", id.getValue());

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                Id.valueOf(null);
            }
        });
    }

    /**
     * 
     */
    @Test
    public void testEqualsAndHashCode() {
        Id<String, String> baseObjToTest = Id.valueOf("id");
        Id<String, String> equalsToBase1 = Id.valueOf("id");
        Id<String, String> equalsToBase2 = Id.valueOf("id");
        Id<String, String> unequalToBase = Id.valueOf("other id");

        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1,
                                             equalsToBase2, unequalToBase);
    }

    /**
     * 
     */
    @Test
    public void testSerialization() {
        SerializabilityTester.testSerialization(Id.valueOf("id"));
    }
}
