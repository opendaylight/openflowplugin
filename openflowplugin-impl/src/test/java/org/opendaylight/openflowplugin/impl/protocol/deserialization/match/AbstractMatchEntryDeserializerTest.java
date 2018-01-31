/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MatchEntryDeserializer;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.AbstractDeserializerTest;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.key.MessageCodeMatchKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagsMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;

public abstract class AbstractMatchEntryDeserializerTest extends AbstractDeserializerTest {

    private MatchEntryDeserializer deserializer;

    @Override
    protected void init() {
        deserializer = getRegistry().getDeserializer(new MessageCodeMatchKey(EncodeConstants.OF13_VERSION_ID,
                EncodeConstants.EMPTY_LENGTH,
                Match.class,
                MatchPath.FLOWS_STATISTICS_UPDATE_MATCH));
    }

    private void deserializeAlreadyFilledCase(ByteBuf inBuffer) {
        final MatchBuilder builder = new MatchBuilder()
                .setProtocolMatchFields(new ProtocolMatchFieldsBuilder().build())
                .setEthernetMatch(new EthernetMatchBuilder().build())
                .setInPhyPort(new NodeConnectorId(""))
                .setInPort(new NodeConnectorId(""))
                .setLayer3Match(new Ipv4MatchBuilder().build())
                .setIpMatch(new IpMatchBuilder().build())
                .setMetadata(new MetadataBuilder().build())
                .setTunnel(new TunnelBuilder().build())
                .setVlanMatch(new VlanMatchBuilder().build())
                .setTcpFlagsMatch(new TcpFlagsMatchBuilder().build())
                .setIcmpv4Match(new Icmpv4MatchBuilder().build())
                .setIcmpv6Match(new Icmpv6MatchBuilder().build())
                .setLayer4Match(new TcpMatchBuilder().build());

        try {
            deserializer.deserializeEntry(inBuffer, builder);
        } catch (final IllegalArgumentException ignored) {
            // Illegal argument exception is expected here, just ignore it
        }
    }

    protected Match deserialize(ByteBuf inBuffer) {
        deserializeAlreadyFilledCase(inBuffer.copy());
        final MatchBuilder builder = new MatchBuilder();
        deserializer.deserializeEntry(inBuffer, builder);
        return builder.build();
    }

    protected void writeHeader(ByteBuf inBuffer, boolean hasMask) {
        inBuffer.writeShort(getOxmClassCode());

        int fieldAndMask = getOxmFieldCode() << 1;
        int length = getValueLength();

        if (hasMask) {
            fieldAndMask |= 1;
            length *= 2;
        }

        inBuffer.writeByte(fieldAndMask);
        inBuffer.writeByte(length);
    }

    protected abstract int getOxmClassCode();

    protected abstract int getOxmFieldCode();

    protected abstract int getValueLength();

}
