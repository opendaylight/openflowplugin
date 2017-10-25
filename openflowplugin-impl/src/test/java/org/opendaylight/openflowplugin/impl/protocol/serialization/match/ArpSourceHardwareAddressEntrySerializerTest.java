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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;

public class ArpSourceHardwareAddressEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final MacAddress address = new MacAddress("00:01:02:03:04:05");
        final MacAddress mask = new MacAddress("00:00:00:00:00:00");

        final Match arpShaMatch = new MatchBuilder()
                .setLayer3Match(new ArpMatchBuilder()
                        .setArpSourceHardwareAddress(new ArpSourceHardwareAddressBuilder()
                                .setAddress(address)
                                .setMask(mask)
                                .build())
                        .build())
                .build();

        assertMatch(arpShaMatch, true, (out) -> {
            byte[] addressBytes = new byte[6];
            out.readBytes(addressBytes);
            assertEquals(new MacAddress(ByteBufUtils.macAddressToString(addressBytes)).getValue(), address.getValue());

            byte[] maskBytes = new byte[6];
            out.readBytes(maskBytes);
            assertEquals(new MacAddress(ByteBufUtils.macAddressToString(maskBytes)).getValue(), mask.getValue());
        });

        final Match arpShaMatchNoMask = new MatchBuilder()
                .setLayer3Match(new ArpMatchBuilder()
                        .setArpSourceHardwareAddress(new ArpSourceHardwareAddressBuilder()
                                .setAddress(address)
                                .build())
                        .build())
                .build();

        assertMatch(arpShaMatchNoMask, false, (out) -> {
            byte[] addressBytes = new byte[6];
            out.readBytes(addressBytes);
            assertEquals(new MacAddress(ByteBufUtils.macAddressToString(addressBytes)).getValue(), address.getValue());
        });
    }

    @Override
    protected short getLength() {
        return EncodeConstants.MAC_ADDRESS_LENGTH;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.ARP_SHA;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

}
