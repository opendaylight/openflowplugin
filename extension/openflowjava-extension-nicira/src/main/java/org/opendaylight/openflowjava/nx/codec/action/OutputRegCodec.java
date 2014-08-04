/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.NxmNxOutputReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.output.reg.grouping.ActionOutputReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.output.reg.grouping.ActionOutputRegBuilder;

/**
 * Codec for the Nicira OutputRegAction
 * @author readams
 */
public class OutputRegCodec extends AbstractActionCodec {
    public static final int LENGTH = 24;
    public static final byte SUBTYPE = 15; // NXAST_OUTPUT_REG
    public static final NiciraActionSerializerKey SERIALIZER_KEY = 
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, NxmNxOutputReg.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY = 
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, SUBTYPE);

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionOutputReg action = input.getAugmentation(OfjAugNxAction.class).getActionOutputReg();
        serializeHeader(LENGTH, SUBTYPE, outBuffer);
        outBuffer.writeShort(action.getNBits().shortValue());
        outBuffer.writeInt(action.getSrc().intValue());
        outBuffer.writeShort(action.getMaxLen().shortValue());
        outBuffer.writeZero(6);
    }

    @Override
    public Action deserialize(ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);        
        ActionOutputRegBuilder builder = new ActionOutputRegBuilder();
        builder.setNBits(message.readUnsignedShort());
        builder.setSrc(message.readUnsignedInt());
        builder.setMaxLen(message.readUnsignedShort());
        OfjAugNxActionBuilder augNxActionBuilder = new OfjAugNxActionBuilder();
        augNxActionBuilder.setActionOutputReg(builder.build());
        actionBuilder.addAugmentation(ExperimenterIdAction.class, 
                                      createExperimenterIdAction(NxmNxOutputReg.class));
        actionBuilder.addAugmentation(OfjAugNxAction.class, augNxActionBuilder.build());
        return actionBuilder.build();
    }

}
