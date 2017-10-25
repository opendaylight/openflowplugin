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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCase;

/**
 * @author michal.polkorab
 *
 */
public class InstructionSerializerKeyTest {

    /**
     * Test InstructionSerializerKey equals and hashCode
     */
    @Test
    public void test() {
        InstructionSerializerKey<?> key1 =
                new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, 42L);
                InstructionSerializerKey<?> key2 =
                new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, 42L);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, null, 42L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, WriteActionsCase.class, 42L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, 55L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionSerializerKey<>(EncodeConstants.OF13_VERSION_ID, ApplyActionsCase.class, 42L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
    }

    /**
     * Test InstructionSerializerKey equals  - additional test
     */
    @Test
    public void testEquals(){
        InstructionSerializerKey<?> key1 =
                new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, 42L);
        InstructionSerializerKey<?> key2 =
                new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, 42L);

        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        Assert.assertFalse("Wrong equal to different class.", key1.equals(new Object()));

        key1 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, null);
        Assert.assertFalse("Wrong equal by experimenterId.", key1.equals(key2));

        key1 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, null, 42L);
        key2 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, null, 42L);
        Assert.assertTrue("Wrong equal by instructionType.", key1.equals(key2));
    }

    /**
     * Test InstructionSerializerKey toString()
     */
    @Test
    public void testToString(){
        InstructionSerializerKey<?> key1 =
                new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, 42L);

        Assert.assertEquals("Wrong toString()", "msgVersion: 1 objectType: org.opendaylight.yang.gen.v1.urn"
                + ".opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction "
                + "instructionType type: org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction"
                + ".rev130731.instruction.grouping.instruction.choice.ApplyActionsCase vendorID: 42", key1.toString());
    }
}