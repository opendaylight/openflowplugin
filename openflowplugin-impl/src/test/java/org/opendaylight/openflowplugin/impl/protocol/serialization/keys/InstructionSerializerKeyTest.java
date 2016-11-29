/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.keys;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;

public class InstructionSerializerKeyTest {

    @Test
    public void testHashCode() throws Exception {
        InstructionSerializerKey<ApplyActionsCase> key1 =
                new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, 42L);
        InstructionSerializerKey<?> key2 =
                new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, 42L);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, null, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, WriteActionsCase.class, 42L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, 55L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
        key2 = new InstructionSerializerKey<>(EncodeConstants.OF13_VERSION_ID, ApplyActionsCase.class, 55L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode", key1.hashCode() == key2.hashCode());
    }

    @Test
    public void testEquals() throws Exception {
        InstructionSerializerKey<?> key1 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, null, 42L);
        InstructionSerializerKey<?> key2 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                ApplyActionsCase.class, 42L);

        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        Assert.assertFalse("Wrong equal by instructionType", key1.equals(key2));

        key2 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, null, 42L);
        Assert.assertTrue("Wrong equal by instruction type", key1.equals(key2));
        key1 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID,  ApplyActionsCase.class, null);
        Assert.assertFalse("Wrong equal by experimenterId", key1.equals(key2));
        key2 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID, ApplyActionsCase.class, null);
        Assert.assertTrue("Wrong equal by experimenterId", key1.equals(key2));
    }

    @Test
    public void testToString() throws Exception {
        InstructionSerializerKey<ApplyActionsCase> key1 = new InstructionSerializerKey<>(EncodeConstants.OF10_VERSION_ID,
                ApplyActionsCase.class, 42L);

        Assert.assertEquals("Wrong toString()", "msgVersion: 1 objectType: org.opendaylight.yang.gen.v1.urn."
                + "opendaylight.flow.types.rev131026.instruction.Instruction instruction type:"
                + " org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction"
                + ".ApplyActionsCase experimenterID: 42", key1.toString());
    }

}