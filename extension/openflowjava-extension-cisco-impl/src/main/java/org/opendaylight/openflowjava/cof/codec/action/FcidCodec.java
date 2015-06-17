/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.cof.codec.action;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.cof.api.CiscoActionDeserializerKey;
import org.opendaylight.openflowjava.cof.api.CiscoActionSerializerKey;
import org.opendaylight.openflowjava.cof.codec.CodecUtil;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofActionFcid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionFcid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionFcidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.fcid.grouping.ActionFcid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.fcid.grouping.ActionFcidBuilder;

/**
 * Created by Martin Bobak mbobak@cisco.com on 10/23/14.
 */
public class FcidCodec extends AbstractActionCodec {

    private static final FcidCodec instance = new FcidCodec();
    public static final byte SUBTYPE = 3;

    /**
     * serializer key for VRF action
     */
    public static final CiscoActionSerializerKey SERIALIZER_KEY = new CiscoActionSerializerKey(
            EncodeConstants.OF13_VERSION_ID, OfjCofActionFcid.class);
    /**
     * deserializer key for VRF action
     */
    public static final CiscoActionDeserializerKey DESERIALIZER_KEY = new CiscoActionDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, SUBTYPE);

    /**
     * serializer key for VRF action, OF-1.0
     */
    public static final CiscoActionSerializerKey SERIALIZER_KEY_10 = new CiscoActionSerializerKey(
            EncodeConstants.OF10_VERSION_ID, OfjCofActionFcid.class);
    /**
     * deserializer key for VRF action OF-1.0
     */
    public static final CiscoActionDeserializerKey DESERIALIZER_KEY_10 = new CiscoActionDeserializerKey(
            EncodeConstants.OF10_VERSION_ID, SUBTYPE);


    @Override
    public Action deserialize(ByteBuf byteBuf) {
        ActionFcidBuilder actionFcidBuilder = new ActionFcidBuilder();
        ActionBuilder actionBuilder = deserializeHeader(byteBuf);
        actionFcidBuilder.setFcid(byteBuf.readUnsignedByte());
        OfjCofActionFcidBuilder ofjAugCofActionBuilder = new OfjCofActionFcidBuilder();
        ofjAugCofActionBuilder.setActionFcid(actionFcidBuilder.build());
        actionBuilder.setActionChoice(ofjAugCofActionBuilder.build());
        return actionBuilder.build();
    }


    /**
     * <pre>
     *    struct cof_action_fcid {
     *        ovs_be16 type;              / * OFPAT_VENDOR * /
     *        ovs_be16 len;               / * Length is 0 * /
     *        ovs_be32 vendor;            / * CISCO_VENDOR_ID * /
     *        ovs_be16 subtype;           / * COF_AT_SET_FORWARD_CLASS * /
     *        uint8_t  fcid;              / * TUNNEL_POLICY_CLASS_MAX = 7 * /
     *        uint8_t  pad[5];
     *    };
     * </pre>
     */
    @Override
    public void serialize(Action action, ByteBuf byteBuf) {
        int startIdx = byteBuf.writerIndex();

        ActionFcid actionFcid = ((OfjCofActionFcid) action.getActionChoice()).getActionFcid();
        serializeHeader(16, SUBTYPE, byteBuf);
        byteBuf.writeByte(actionFcid.getFcid());
        byteBuf.writeZero(CodecUtil.computePadding8(byteBuf.writerIndex() - startIdx));
    }


    /**
     * @return singleton
     */
    public static FcidCodec getInstance() {
        return instance;
    }

}
