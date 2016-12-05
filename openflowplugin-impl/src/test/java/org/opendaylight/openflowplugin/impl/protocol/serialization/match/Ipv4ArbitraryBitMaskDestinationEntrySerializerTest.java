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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMaskBuilder;

public class Ipv4ArbitraryBitMaskDestinationEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final Ipv4Address ipv4Address = new Ipv4Address("192.168.10.0");
        final DottedQuad ipv4mask = new DottedQuad("255.255.255.0");

        final Match ipv4abmMatch = new MatchBuilder()
                .setLayer3Match(new Ipv4MatchArbitraryBitMaskBuilder()
                        .setIpv4DestinationAddressNoMask(ipv4Address)
                        .setIpv4DestinationArbitraryBitmask(ipv4mask)
                        .build())
                .build();

        assertMatch(ipv4abmMatch, true, (out) -> {
            byte[] address = new byte[4];
            out.readBytes(address);
            assertArrayEquals(address, new byte[]{ (byte) 192, (byte) 168, 10, 0 });

            byte[] mask = new byte[4];
            out.readBytes(mask);
            assertArrayEquals(mask, new byte[]{ (byte) 255, (byte) 255, (byte) 255, 0 });
        });

        final Match ipv4abmMatchNoMask = new MatchBuilder()
                .setLayer3Match(new Ipv4MatchArbitraryBitMaskBuilder()
                        .setIpv4DestinationAddressNoMask(ipv4Address)
                        .build())
                .build();

        assertMatch(ipv4abmMatchNoMask, false, (out) -> {
            byte[] address = new byte[4];
            out.readBytes(address);
            assertArrayEquals(address, new byte[]{ (byte) 192, (byte) 168, 10, 0 });
        });
    }

    @Override
    protected short getLength() {
        return EncodeConstants.SIZE_OF_INT_IN_BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IPV4_DST;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

}
