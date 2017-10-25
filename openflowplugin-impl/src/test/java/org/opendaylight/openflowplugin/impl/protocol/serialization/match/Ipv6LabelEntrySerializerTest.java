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
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6LabelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;

public class Ipv6LabelEntrySerializerTest extends AbstractMatchEntrySerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final long ipv6flowLabel = 358;
        final long ipv6flowLabelMask = 100;

        final Match ipv6flowLabelMatch = new MatchBuilder()
                .setLayer3Match(new Ipv6MatchBuilder()
                        .setIpv6Label(new Ipv6LabelBuilder()
                                .setIpv6Flabel(new Ipv6FlowLabel(ipv6flowLabel))
                                .setFlabelMask(new Ipv6FlowLabel(ipv6flowLabelMask))
                                .build())
                        .build())
                .build();

        assertMatch(ipv6flowLabelMatch, true, (out) -> {
            assertEquals(out.readUnsignedInt(), ipv6flowLabel);

            byte[] mask = new byte[4];
            out.readBytes(mask);
            assertArrayEquals(mask, ByteUtil.unsignedIntToBytes(ipv6flowLabelMask));
        });

        final Match ipv6exyHdrMatchNoMask = new MatchBuilder()
                .setLayer3Match(new Ipv6MatchBuilder()
                        .setIpv6Label(new Ipv6LabelBuilder()
                                .setIpv6Flabel(new Ipv6FlowLabel(ipv6flowLabel))
                                .build())
                        .build())
                .build();

        assertMatch(ipv6exyHdrMatchNoMask, false, (out) -> assertEquals(out.readUnsignedInt(), ipv6flowLabel));
    }

    @Override
    protected short getLength() {
        return EncodeConstants.SIZE_OF_INT_IN_BYTES;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IPV6_FLABEL;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

}
