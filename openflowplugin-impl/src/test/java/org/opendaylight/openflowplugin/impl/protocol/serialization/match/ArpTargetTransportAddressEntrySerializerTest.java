/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;

public class ArpTargetTransportAddressEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void testSerialize() {
        final Match arpSpaMatch = new MatchBuilder()
                .setLayer3Match(new ArpMatchBuilder()
                        .setArpTargetTransportAddress(new Ipv4Prefix("10.0.2.0/24"))
                        .build())
                .build();

        assertMatch(arpSpaMatch, true, (out) -> {
            byte[] address = new byte[4];
            out.readBytes(address);
            assertArrayEquals(address, new byte[]{ 10, 0, 2, 0 });

            byte[] mask = new byte[4];
            out.readBytes(mask);
            assertArrayEquals(mask, new byte[]{ (byte) 255, (byte) 255, (byte) 255, 0 });
        });

        final Match arpSpaMatchNoMask = new MatchBuilder()
                .setLayer3Match(new ArpMatchBuilder()
                        .setArpTargetTransportAddress(new Ipv4Prefix("10.0.0.0/32"))
                        .build())
                .build();

        assertMatch(arpSpaMatchNoMask, false, (out) -> {
            byte[] address = new byte[4];
            out.readBytes(address);
            assertArrayEquals(address, new byte[]{ 10, 0, 0, 0 });
        });
    }

    @Override
    protected short getLength() {
        return EncodeConstants.SIZE_OF_INT_IN_BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.ARP_TPA;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

}
