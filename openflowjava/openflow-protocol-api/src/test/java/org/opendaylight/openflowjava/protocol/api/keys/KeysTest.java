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
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.action.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.ExperimenterActionSubType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for keys.
 *
 * @author michal.polkorab
 */
public class KeysTest {

    /**
     * Testing equals() and hashcode() methods of extension deserializer's keys.
     */
    @Test
    public void testEqualsAndHashcodeOfDeserializerKeys() {
        ActionDeserializerKey actionDeserializerKey = new ActionDeserializerKey(EncodeConstants.OF_VERSION_1_3,
                EncodeConstants.EXPERIMENTER_VALUE, 1L);
        ExperimenterActionDeserializerKey experimenterActionDeserializerKey = new ExperimenterActionDeserializerKey(
                EncodeConstants.OF_VERSION_1_3, 1L);
        Assert.assertEquals(actionDeserializerKey, experimenterActionDeserializerKey);
        Assert.assertEquals(actionDeserializerKey.hashCode(), experimenterActionDeserializerKey.hashCode());

        InstructionDeserializerKey instructionDeserializerKey = new InstructionDeserializerKey(
                EncodeConstants.OF_VERSION_1_3, EncodeConstants.EXPERIMENTER_VALUE, 1L);
        ExperimenterInstructionDeserializerKey experimenterInstructionDeserializerKey =
                new ExperimenterInstructionDeserializerKey(EncodeConstants.OF_VERSION_1_3, 1L);
        Assert.assertEquals(instructionDeserializerKey, experimenterInstructionDeserializerKey);
        Assert.assertEquals(instructionDeserializerKey.hashCode(), experimenterInstructionDeserializerKey.hashCode());

        MatchEntryDeserializerKey matchKey = new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ARP_OP);
        MatchEntryDeserializerKey matchKey2 = new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_3,
                OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ARP_OP);
        Assert.assertEquals(matchKey, matchKey2);
        Assert.assertEquals(matchKey.hashCode(), matchKey2.hashCode());
    }

    /**
     * Testing equals() and hashcode() methods of extension serializer's keys.
     */
    @Test
    public void testEqualsAndHashcodeOfActionDeserializerKeys() {
        ActionSerializerKey<ExperimenterIdCase> actionSerializerKey = new ActionSerializerKey<>(
                EncodeConstants.OF_VERSION_1_3, ExperimenterIdCase.class, Uint32.ONE);
        ExperimenterActionSerializerKey experimenterActionSerializerKey = new ExperimenterActionSerializerKey(
                EncodeConstants.OF_VERSION_1_3,  Uint32.ONE, ExpSubType.VALUE);
        Assert.assertFalse(actionSerializerKey.equals(experimenterActionSerializerKey));
        Assert.assertFalse(experimenterActionSerializerKey.equals(actionSerializerKey));

        InstructionSerializerKey<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225
            .instruction.container.instruction.choice.ExperimenterIdCase> instructionSerializerKey =
                new InstructionSerializerKey<>(EncodeConstants.OF_VERSION_1_3, org.opendaylight.yang.gen.v1.urn
                        .opendaylight.openflow.augments.rev150225.instruction.container.instruction.choice
                        .ExperimenterIdCase.class, 1L);
        ExperimenterInstructionSerializerKey experimenterInstructionSerializerKey =
                new ExperimenterInstructionSerializerKey(EncodeConstants.OF_VERSION_1_3, 1L);
        Assert.assertEquals(instructionSerializerKey, experimenterInstructionSerializerKey);
        Assert.assertEquals(instructionSerializerKey.hashCode(), experimenterInstructionSerializerKey.hashCode());

        MatchEntrySerializerKey<OpenflowBasicClass, InPort> matchKey = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_0, OpenflowBasicClass.VALUE, InPort.VALUE);
        MatchEntrySerializerKey<OpenflowBasicClass, InPort> matchKey2 = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_0, OpenflowBasicClass.VALUE, InPort.VALUE);
        Assert.assertEquals(matchKey, matchKey2);
        Assert.assertEquals(matchKey.hashCode(), matchKey2.hashCode());
    }

    private interface ExpSubType extends ExperimenterActionSubType {
        ExpSubType VALUE = () -> ExpSubType.class;

        @Override
        Class<? extends ExpSubType> implementedInterface();
    }
}
