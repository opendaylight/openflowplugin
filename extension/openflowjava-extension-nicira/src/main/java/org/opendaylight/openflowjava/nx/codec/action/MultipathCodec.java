/*
 * Copyright (c) 2014 SDN Hub, LLC. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjNxHashFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjNxMpAlgorithm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionMultipathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.multipath.grouping.NxActionMultipathBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Codec for the NX_MULTIPATH
 */
public class MultipathCodec extends AbstractActionCodec {
    public static final int LENGTH = 32;
    public static final byte NXAST_MULTIPATH_SUBTYPE = 10;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionMultipath.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_MULTIPATH_SUBTYPE);

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        ActionMultipath action = ((ActionMultipath) input.getActionChoice());
        serializeHeader(LENGTH, NXAST_MULTIPATH_SUBTYPE, outBuffer);

        outBuffer.writeShort(action.getNxActionMultipath().getFields().getIntValue());
        outBuffer.writeShort(action.getNxActionMultipath().getBasis().shortValue());
        outBuffer.writeZero(2);

        outBuffer.writeShort(action.getNxActionMultipath().getAlgorithm().getIntValue());
        outBuffer.writeShort(action.getNxActionMultipath().getMaxLink().shortValue());
        outBuffer.writeInt(action.getNxActionMultipath().getArg().intValue());
        outBuffer.writeZero(2);

        outBuffer.writeShort(action.getNxActionMultipath().getOfsNbits().shortValue());
        outBuffer.writeInt(action.getNxActionMultipath().getDst().intValue());
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionMultipathBuilder actionMultipathBuilder = new ActionMultipathBuilder();

        NxActionMultipathBuilder nxActionMultipathBuilder = new NxActionMultipathBuilder();
        nxActionMultipathBuilder.setFields(OfjNxHashFields.forValue(message.readUnsignedShort()));
        nxActionMultipathBuilder.setBasis(message.readUnsignedShort());
        message.skipBytes(2); //two bytes

        nxActionMultipathBuilder.setAlgorithm(OfjNxMpAlgorithm.forValue(message.readUnsignedShort()));
        nxActionMultipathBuilder.setMaxLink(message.readUnsignedShort());
        nxActionMultipathBuilder.setArg(message.readUnsignedInt());
        message.skipBytes(2); //two bytes

        nxActionMultipathBuilder.setOfsNbits(message.readUnsignedShort());
        nxActionMultipathBuilder.setDst(message.readUnsignedInt());
        actionMultipathBuilder.setNxActionMultipath(nxActionMultipathBuilder.build());
        actionBuilder.setActionChoice(actionMultipathBuilder.build());

        return actionBuilder.build();
    }
}
