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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;

public class Ipv6ArbitraryBitMaskDestinationEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final Ipv6Address ipv6Address = new Ipv6Address("aaaa:bbbb:1111:2222::");
        final Ipv6ArbitraryMask ipv6mask = new Ipv6ArbitraryMask("ffff:ffff:ffff:ffff::");

        final Match ipv6abmMatch = new MatchBuilder()
                .setLayer3Match(new Ipv6MatchArbitraryBitMaskBuilder()
                        .setIpv6DestinationAddressNoMask(ipv6Address)
                        .setIpv6DestinationArbitraryBitmask(ipv6mask)
                        .build())
                .build();

        assertMatch(ipv6abmMatch, true, (out) -> {
            byte[] address = new byte[16];
            out.readBytes(address);
            assertArrayEquals(address, IetfInetUtil.INSTANCE.ipv6AddressBytes(ipv6Address));

            byte[] mask = new byte[16];
            out.readBytes(mask);
            assertArrayEquals(mask, IpConversionUtil.convertIpv6ArbitraryMaskToByteArray(ipv6mask));
        });

        final Match ipv6abmMatchNoMask = new MatchBuilder()
                .setLayer3Match(new Ipv6MatchArbitraryBitMaskBuilder()
                        .setIpv6DestinationAddressNoMask(ipv6Address)
                        .build())
                .build();

        assertMatch(ipv6abmMatchNoMask, false, (out) -> {
            byte[] address = new byte[16];
            out.readBytes(address);
            assertArrayEquals(address, IetfInetUtil.INSTANCE.ipv6AddressBytes(ipv6Address));
        });
    }

    @Override
    protected short getLength() {
        return EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IPV6_DST;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

}
