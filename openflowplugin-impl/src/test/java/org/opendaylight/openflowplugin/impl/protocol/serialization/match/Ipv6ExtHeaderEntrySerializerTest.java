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
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6ExtHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

public class Ipv6ExtHeaderEntrySerializerTest extends AbstractMatchEntrySerializerTest {
    @Test
    public void testSerialize() {
        final Uint16 ipv6extHdr = Uint16.valueOf(358);
        final Uint16 ipv6extHdrMask = Uint16.valueOf(100);

        final Match ipv6extHdrMatch = new MatchBuilder()
                .setLayer3Match(new Ipv6MatchBuilder()
                        .setIpv6ExtHeader(new Ipv6ExtHeaderBuilder()
                                .setIpv6Exthdr(ipv6extHdr)
                                .setIpv6ExthdrMask(ipv6extHdrMask)
                                .build())
                        .build())
                .build();

        assertMatch(ipv6extHdrMatch, true, (out) -> {
            assertEquals(out.readUnsignedShort(), ipv6extHdr.intValue());

            byte[] mask = new byte[2];
            out.readBytes(mask);
            assertArrayEquals(mask, ByteUtil.unsignedShortToBytes(ipv6extHdrMask));
        });

        final Match ipv6exyHdrMatchNoMask = new MatchBuilder()
                .setLayer3Match(new Ipv6MatchBuilder()
                        .setIpv6ExtHeader(new Ipv6ExtHeaderBuilder()
                                .setIpv6Exthdr(ipv6extHdr)
                                .build())
                        .build())
                .build();

        assertMatch(ipv6exyHdrMatchNoMask, false,
            (out) -> assertEquals(out.readUnsignedShort(), ipv6extHdr.intValue()));
    }

    @Override
    protected short getLength() {
        return Short.BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IPV6_EXTHDR;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }
}
