/*
 * Copyright (C) 2014 SDN Hub, LLC.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Authors : Srini Seetharaman
 */

package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.openflowjava.nx.codec.action.AbstractActionCodec;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.NxmNxMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjNxMpAlgorithm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjNxHashFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.multipath.grouping.ActionMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.multipath.grouping.ActionMultipathBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Codec for the NX_MULTIPATH
 */
public class MultipathCodec extends AbstractActionCodec {
    private static final Logger logger = LoggerFactory.getLogger(MultipathCodec.class);

    public static final int LENGTH = 32;
    public static final byte NXAST_MULTIPATH_SUBTYPE = 10;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, NxmNxMultipath.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_MULTIPATH_SUBTYPE);

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionMultipath action = input.getAugmentation(OfjAugNxAction.class).getActionMultipath();
        serializeHeader(LENGTH, NXAST_MULTIPATH_SUBTYPE, outBuffer);

        outBuffer.writeShort(action.getFields().getIntValue());
        outBuffer.writeShort(action.getBasis().shortValue());
        outBuffer.writeZero(2);

        outBuffer.writeShort(action.getAlgorithm().getIntValue());
        outBuffer.writeShort(action.getMaxLink().shortValue());
        outBuffer.writeInt(action.getArg().intValue());
        outBuffer.writeZero(2);

        outBuffer.writeShort(action.getOfsNbits().shortValue());
        outBuffer.writeInt(action.getDst().intValue());
    }

    @Override
    public Action deserialize(ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionMultipathBuilder builder = new ActionMultipathBuilder();
        builder.setFields(OfjNxHashFields.forValue(message.readUnsignedShort()));
        builder.setBasis(message.readUnsignedShort());
        message.skipBytes(2); //two bytes

        builder.setAlgorithm(OfjNxMpAlgorithm.forValue(message.readUnsignedShort()));
        builder.setMaxLink(message.readUnsignedShort());
        builder.setArg(message.readUnsignedInt());
        message.skipBytes(2); //two bytes

        builder.setOfsNbits(message.readUnsignedShort());
        builder.setDst(message.readUnsignedInt());

        OfjAugNxActionBuilder augNxActionBuilder = new OfjAugNxActionBuilder();
        augNxActionBuilder.setActionMultipath(builder.build());
        actionBuilder.addAugmentation(ExperimenterIdAction.class,
                                      createExperimenterIdAction(NxmNxMultipath.class));
        actionBuilder.addAugmentation(OfjAugNxAction.class, augNxActionBuilder.build());
        return actionBuilder.build();
    }
}
