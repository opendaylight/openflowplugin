/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;

/**
 * Translates FeaturesReply messages.
 *
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class FeaturesReplyMessageFactory implements OFDeserializer<GetFeaturesOutput> {

    private static final byte PADDING_IN_FEATURES_REPLY_HEADER = 2;

    @Override
    public GetFeaturesOutput deserialize(ByteBuf rawMessage) {
        GetFeaturesOutputBuilder builder = new GetFeaturesOutputBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        byte[] datapathId = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        rawMessage.readBytes(datapathId);
        builder.setDatapathId(new BigInteger(1, datapathId));
        builder.setBuffers(rawMessage.readUnsignedInt());
        builder.setTables(rawMessage.readUnsignedByte());
        builder.setAuxiliaryId(rawMessage.readUnsignedByte());
        rawMessage.skipBytes(PADDING_IN_FEATURES_REPLY_HEADER);
        builder.setCapabilities(createCapabilities(rawMessage.readUnsignedInt()));
        builder.setReserved(rawMessage.readUnsignedInt());
        return builder.build();
    }

    private static Capabilities createCapabilities(long input) {
        final Boolean flowStats = (input & 1 << 0) != 0;
        final Boolean tableStats = (input & 1 << 1) != 0;
        final Boolean portStats = (input & 1 << 2) != 0;
        final Boolean groupStats = (input & 1 << 3) != 0;
        final Boolean ipReasm = (input & 1 << 5) != 0;
        final Boolean queueStats = (input & 1 << 6) != 0;
        final Boolean portBlocked = (input & 1 << 8) != 0;
        return new Capabilities(flowStats, groupStats, ipReasm,
                portBlocked, portStats, queueStats, tableStats);
    }

}
