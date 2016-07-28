/*
 * Copyright (c) 2016 Hewlett-Packard Enterprise and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionLearn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionLearnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.NxActionLearnBuilder;

/**
 * @author Slava Radune
 */

public class LearnCodec extends AbstractActionCodec {
    public static final byte NXAST_LEARN_SUBTYPE = 16;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionLearn.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_LEARN_SUBTYPE);

    private short length;

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        ActionLearn action = (ActionLearn) input.getActionChoice();
        int length = LearnCodecUtil.calcLength(action);
        int lengthMod = length % 8;
        if(lengthMod != 0){
            lengthMod = 8 - lengthMod;
        }
        serializeHeader(length+lengthMod, NXAST_LEARN_SUBTYPE, outBuffer);

        LearnCodecUtil.serializeLearnHeader(outBuffer, action);
        LearnCodecUtil.serializeFlowMods(outBuffer, action);
        
        //pad with zeros for the length to be multiplication by 8
        if(lengthMod != 0){
            outBuffer.writeZero(lengthMod);
        }
        
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionLearnBuilder actionLearnBuilder = new ActionLearnBuilder();

        NxActionLearnBuilder nxActionLearnBuilder = new NxActionLearnBuilder();
        LearnCodecUtil.deserializeLearnHeader(message, nxActionLearnBuilder);
        
        length -= LearnCodecUtil.HEADER_LENGTH;
        
        LearnCodecUtil.buildFlowModSpecs(nxActionLearnBuilder, message, length);
        
        actionLearnBuilder.setNxActionLearn(nxActionLearnBuilder.build());

        actionBuilder.setActionChoice(actionLearnBuilder.build());

        return actionBuilder.build();
    }

    @Override
    protected ActionBuilder deserializeHeader(ByteBuf message) {
        // size of experimenter type
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        // size of length
        length = message.readShort();
        // vendor id
        message.skipBytes(EncodeConstants.SIZE_OF_INT_IN_BYTES);
        // subtype
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(getExperimenterId());
        return actionBuilder;
    }
}
