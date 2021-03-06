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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagsMatchBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

public class TcpFlagsEntrySerializerTest extends AbstractExperimenterMatchEntrySerializerTest {

    @Test
    public void testSerialize() {
        final Uint16 tcp = Uint16.valueOf(8);
        final byte[] tcpMask = new byte[] { 30, 30 };

        final Match tcpFlagsMatch = new MatchBuilder()
                .setTcpFlagsMatch(new TcpFlagsMatchBuilder()
                        .setTcpFlags(tcp)
                        .setTcpFlagsMask(Uint16.valueOf(0x1E1E))
                        .build())
                .build();

        assertMatch(tcpFlagsMatch, true, (out) -> {
            assertEquals(out.readUnsignedShort(), tcp.intValue());

            byte[] mask = new byte[2];
            out.readBytes(mask);
            assertArrayEquals(mask, tcpMask);
        });

        final Match tcpFlagsMatchNoMask = new MatchBuilder()
                .setTcpFlagsMatch(new TcpFlagsMatchBuilder()
                        .setTcpFlags(tcp)
                        .build())
                .build();

        assertMatch(tcpFlagsMatchNoMask, false, (out) -> assertEquals(out.readUnsignedShort(), tcp.intValue()));
    }

    @Override
    protected short getLength() {
        return Short.BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return EncodeConstants.ONFOXM_ET_TCP_FLAGS;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.EXPERIMENTER_CLASS;
    }

    @Override
    protected long getExperimenterId() {
        return EncodeConstants.ONF_EXPERIMENTER_ID.toJava();
    }
}
