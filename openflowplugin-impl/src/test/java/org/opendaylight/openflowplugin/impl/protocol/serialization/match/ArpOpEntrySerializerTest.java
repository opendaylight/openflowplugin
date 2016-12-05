/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;

public class ArpOpEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final int arpOp = 42;

        final Match arpOpMatch = new MatchBuilder()
                .setLayer3Match(new ArpMatchBuilder()
                        .setArpOp(arpOp)
                        .build())
                .build();

        assertMatch(arpOpMatch, false, (out) -> assertEquals(out.readShort(), arpOp));
    }

    @Override
    protected short getLength() {
        return EncodeConstants.SIZE_OF_SHORT_IN_BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.ARP_OP;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

}