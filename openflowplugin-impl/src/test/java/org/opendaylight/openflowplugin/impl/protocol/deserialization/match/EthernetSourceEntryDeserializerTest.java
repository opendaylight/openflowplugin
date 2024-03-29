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
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSource;

public class EthernetSourceEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {

    @Test
    public void deserializeEntry() {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final MacAddress ethernetSourceAddress = new MacAddress("00:01:02:03:04:05");
        final MacAddress ethernetSourceAddressMask = new MacAddress("00:00:00:00:00:00");

        writeHeader(in, false);
        in.writeBytes(IetfYangUtil.macAddressBytes(ethernetSourceAddress));

        assertEquals(ethernetSourceAddress.getValue(),
                deserialize(in).getEthernetMatch().getEthernetSource().getAddress().getValue());
        assertEquals(0, in.readableBytes());

        writeHeader(in, true);
        in.writeBytes(IetfYangUtil.macAddressBytes(ethernetSourceAddress));
        in.writeBytes(IetfYangUtil.macAddressBytes(ethernetSourceAddressMask));

        final EthernetSource desAddress = deserialize(in).getEthernetMatch().getEthernetSource();
        assertEquals(ethernetSourceAddress.getValue(), desAddress.getAddress().getValue());
        assertEquals(ethernetSourceAddressMask.getValue(), desAddress.getMask().getValue());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.ETH_SRC;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.MAC_ADDRESS_LENGTH;
    }
}
