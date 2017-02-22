/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.Iterator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;

public class ArpSourceTransportAddressEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {

    @Test
    public void deserializeEntry() throws Exception {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final Ipv4Prefix arpSourceTransportAddress = new Ipv4Prefix("192.168.0.0/24");
        final Ipv4Prefix arpSourceTransportAddressNoMask = new Ipv4Prefix("192.168.0.0/32");

        writeHeader(in, false);
        Iterator<String> addressParts = IpConversionUtil.splitToParts(arpSourceTransportAddressNoMask);
        in.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(new Ipv4Address(addressParts.next())));

        assertEquals(arpSourceTransportAddressNoMask.getValue(),
                ArpMatch.class.cast(deserialize(in).getLayer3Match()).getArpSourceTransportAddress().getValue());
        assertEquals(0, in.readableBytes());

        writeHeader(in, true);
        addressParts = IpConversionUtil.splitToParts(arpSourceTransportAddress);
        in.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(new Ipv4Address(addressParts.next())));
        in.writeBytes(MatchConvertorUtil.extractIpv4Mask(addressParts));

        final Ipv4Prefix desAddress =
                ArpMatch.class.cast(deserialize(in).getLayer3Match()).getArpSourceTransportAddress();
        assertEquals(arpSourceTransportAddress.getValue(), desAddress.getValue());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.ARP_SPA;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_INT_IN_BYTES;
    }
}
