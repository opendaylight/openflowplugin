/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.actions._case.WriteActionsBuilder;

public class WriteActionsInstructionSerializerTest extends AbstractInstructionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final int order = 0;
        final Ipv4Prefix prefix = new Ipv4Prefix("192.168.76.0/32");

        final Instruction instruction = new WriteActionsCaseBuilder()
                .setWriteActions(new WriteActionsBuilder()
                        .setAction(Collections.singletonList(new ActionBuilder()
                                .setOrder(order)
                                .setKey(new ActionKey(order))
                                .setAction(new SetNwSrcActionCaseBuilder()
                                        .setSetNwSrcAction(new SetNwSrcActionBuilder()
                                                .setAddress(new Ipv4Builder()
                                                        .setIpv4Address(prefix)
                                                .build())
                                        .build())
                                        .build())
                                .build()))
                        .build())
                .build();

        assertInstruction(instruction, out -> {
            out.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);
            assertEquals(out.readUnsignedShort(), ActionConstants.SET_FIELD_CODE);
            out.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES); // Skip length of set field action
            assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
            assertEquals(out.readUnsignedByte(), OxmMatchConstants.IPV4_SRC << 1);
            out.skipBytes(EncodeConstants.SIZE_OF_BYTE_IN_BYTES); // Skip match entry length

            byte[] addressBytes = new byte[4];
            out.readBytes(addressBytes);
            assertArrayEquals(addressBytes, new byte[] { (byte) 192, (byte) 168, 76, 0 });

            out.skipBytes(4); // Padding at end
        });
    }

    @Override
    protected Class<? extends Instruction> getClazz() {
        return WriteActionsCase.class;
    }

    @Override
    protected int getType() {
        return InstructionConstants.WRITE_ACTIONS_TYPE;
    }

    @Override
    protected int getLength() {
        return 24;
    }

}
