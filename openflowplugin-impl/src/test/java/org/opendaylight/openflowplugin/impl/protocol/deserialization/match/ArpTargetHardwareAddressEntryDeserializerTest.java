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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;

public class ArpTargetHardwareAddressEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {

    @Test
    public void deserializeEntry() {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final MacAddress arpTargetHardwareAddress = new MacAddress("00:01:02:03:04:05");
        final MacAddress arpTargetHardwareAddressMask = new MacAddress("00:00:00:00:00:00");

        writeHeader(in, false);
        in.writeBytes(IetfYangUtil.macAddressBytes(arpTargetHardwareAddress));

        assertEquals(arpTargetHardwareAddress.getValue(),
                ((ArpMatch) deserialize(in).getLayer3Match()).getArpTargetHardwareAddress().getAddress()
                        .getValue());
        assertEquals(0, in.readableBytes());

        writeHeader(in, true);
        in.writeBytes(IetfYangUtil.macAddressBytes(arpTargetHardwareAddress));
        in.writeBytes(IetfYangUtil.macAddressBytes(arpTargetHardwareAddressMask));

        final ArpTargetHardwareAddress desAddress =
                ((ArpMatch) deserialize(in).getLayer3Match()).getArpTargetHardwareAddress();
        assertEquals(arpTargetHardwareAddress.getValue(), desAddress.getAddress().getValue());
        assertEquals(arpTargetHardwareAddressMask.getValue(), desAddress.getMask().getValue());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.ARP_THA;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.MAC_ADDRESS_LENGTH;
    }
}
