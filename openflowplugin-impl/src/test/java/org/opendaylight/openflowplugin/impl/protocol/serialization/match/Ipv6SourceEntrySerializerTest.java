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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;

public class Ipv6SourceEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final Ipv6Prefix ipv6Address = new Ipv6Prefix("2001:db8::/32");

        final Match ipv6abmMatch = new MatchBuilder()
                .setLayer3Match(new Ipv6MatchBuilder()
                        .setIpv6Source(ipv6Address)
                        .build())
                .build();

        assertMatch(ipv6abmMatch, true, (out) -> {
            byte[] address = new byte[16];
            out.readBytes(address);
            assertArrayEquals(address, IetfInetUtil.INSTANCE.ipv6AddressBytes(IpConversionUtil.extractIpv6Address(ipv6Address)));

            byte[] mask = new byte[16];
            out.readBytes(mask);
            assertArrayEquals(mask, IpConversionUtil.convertIpv6PrefixToByteArray(IpConversionUtil.extractIpv6Prefix(ipv6Address)));
        });
    }

    @Test
    public void testSerializeWithoutMask() throws Exception {
        final Ipv6Prefix ipv6Address = new Ipv6Prefix("2001:db8::123/128");

        final Match ipv6abmMatch = new MatchBuilder()
                .setLayer3Match(new Ipv6MatchBuilder()
                        .setIpv6Source(ipv6Address)
                        .build())
                .build();

        assertMatch(ipv6abmMatch, false, (out) -> {
            byte[] address = new byte[16];
            out.readBytes(address);
            assertArrayEquals(address, IetfInetUtil.INSTANCE.ipv6AddressBytes(IpConversionUtil.extractIpv6Address(ipv6Address)));
        });
    }

    @Override
    protected short getLength() {
        return EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IPV6_SRC;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

}
