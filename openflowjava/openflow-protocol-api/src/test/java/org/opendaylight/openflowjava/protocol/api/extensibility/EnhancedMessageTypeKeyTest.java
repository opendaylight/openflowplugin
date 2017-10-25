/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.extensibility;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;

/**
 * @author michal.polkorab
 *
 */
public class EnhancedMessageTypeKeyTest {

    /**
     * Test EnhancedMessageTypeKey equals and hashCode
     */
    @Test
    public void test() {
        EnhancedMessageTypeKey<?,?> key1 =
                new EnhancedMessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, Action.class, OutputActionCase.class);
        EnhancedMessageTypeKey<?,?> key2 =
                new EnhancedMessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, Action.class, OutputActionCase.class);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new EnhancedMessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, Action.class, OutputActionCase.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new EnhancedMessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, null, OutputActionCase.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new EnhancedMessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, Instruction.class, OutputActionCase.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new EnhancedMessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, Action.class, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new EnhancedMessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, Action.class, SetFieldCase.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
    }

    /**
     * Test EnhancedMessageTypeKey equals - additional test
     */
    @Test
    public void testEquals() {
        EnhancedMessageTypeKey<?,?> key1;
        EnhancedMessageTypeKey<?,?> key2;
        key1 = new EnhancedMessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, Action.class, OutputActionCase.class);
        key2 = new EnhancedMessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, Action.class, OutputActionCase.class);

        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        Assert.assertFalse("Wrong equal to different class.", key1.equals(new Object()));

        key1 = new EnhancedMessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, Action.class, null);
        Assert.assertFalse("Wrong equal by msgType2.", key1.equals(key2));
        key1 = new EnhancedMessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, Action.class, OutputActionCase.class);
        key2 = new EnhancedMessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, Action.class, SetFieldCase.class);
        Assert.assertFalse("Wrong equal by msgType2 class name.", key1.equals(key2));
    }

    /**
     * Test EnhancedMessageTypeKey toString()
     */
    @Test
    public void testToString() {
        EnhancedMessageTypeKey<?,?> key1 = new EnhancedMessageTypeKey<>(EncodeConstants.OF10_VERSION_ID,
                Action.class, OutputActionCase.class);

        Assert.assertEquals("Wrong toString()", "msgVersion: 1 objectType: org.opendaylight.yang.gen.v1.urn"
                + ".opendaylight.openflow.common.action.rev150203.actions.grouping.Action msgType2:"
                + " org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action"
                + ".grouping.action.choice.OutputActionCase", key1.toString());
    }
}
