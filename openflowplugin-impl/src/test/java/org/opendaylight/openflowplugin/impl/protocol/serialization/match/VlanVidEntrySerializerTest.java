/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;

public class VlanVidEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final int vlan = (short) 1;

        final Match vlanMatch = new MatchBuilder()
                .setVlanMatch(new VlanMatchBuilder()
                        .setVlanId(new VlanIdBuilder()
                                .setVlanId(new VlanId(vlan))
                                .setVlanIdPresent(true)
                                .build())
                        .build())
                .build();

        assertMatch(vlanMatch, false, (out) -> assertEquals(out.readShort(), vlan | (1 << 12)));

        final Match vlanMatchMaskOnly = new MatchBuilder()
                .setVlanMatch(new VlanMatchBuilder()
                        .setVlanId(new VlanIdBuilder()
                                .setVlanIdPresent(true)
                                .build())
                        .build())
                .build();

        assertMatch(vlanMatchMaskOnly, true, out -> {
            assertEquals(out.readShort(), (1 << 12));

            byte mask[] = new byte[2];
            out.readBytes(mask);
            assertArrayEquals(mask, new byte[] { 16, 0 });
        });
    }

    @Override
    protected short getLength() {
        return EncodeConstants.SIZE_OF_SHORT_IN_BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.VLAN_VID;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

}
