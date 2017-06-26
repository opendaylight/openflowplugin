/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.keys;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;

/**
 * @author michal.polkorab
 *
 */
public class InstructionDeserializerKeyTest {

    /**
     * Test InstructionDeserializerKey equals and hashCode
     */
    @Test
    public void test() {
        InstructionDeserializerKey key1 = new InstructionDeserializerKey(EncodeConstants.OF10_VERSION_ID, 11, 42L);
        InstructionDeserializerKey key2 = new InstructionDeserializerKey(EncodeConstants.OF10_VERSION_ID, 11, 42L);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionDeserializerKey(EncodeConstants.OF10_VERSION_ID, 11, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionDeserializerKey(EncodeConstants.OF10_VERSION_ID, 11, 55L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionDeserializerKey(EncodeConstants.OF10_VERSION_ID, 0, 42L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionDeserializerKey(EncodeConstants.OF13_VERSION_ID, 11, 42L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
    }

    /**
     * Test InstructionDeserializerKey equals - additional test
     */
    @Test
    public void testEquals(){
        InstructionDeserializerKey key1 = new InstructionDeserializerKey(EncodeConstants.OF10_VERSION_ID, 11, null);
        InstructionDeserializerKey key2 = new InstructionDeserializerKey(EncodeConstants.OF10_VERSION_ID, 11, 24L);

        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        Assert.assertFalse("Wrong equal to different class.", key1.equals(new Object()));
        Assert.assertFalse("Wrong equal by experimenterId", key1.equals(key2));
        key2 = new InstructionDeserializerKey(EncodeConstants.OF10_VERSION_ID, 11, null);
        Assert.assertTrue("Wrong equal by experimenterId", key1.equals(key2));
    }

    /**
     * Test InstructionDeserializerKey toString()
     */
    @Test
    public void testToString(){
        InstructionDeserializerKey key1 = new InstructionDeserializerKey(EncodeConstants.OF10_VERSION_ID, 11, null);

        Assert.assertEquals("Wrong toString()", "msgVersion: 1 objectClass: org.opendaylight.yang.gen.v1.urn.opendaylight"
                + ".openflow.common.instruction.rev130731.instructions.grouping.Instruction msgType: 11"
                + " experimenterID: null", key1.toString());
    }
}