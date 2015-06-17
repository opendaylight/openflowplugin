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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofActionMplsLsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionMplsLsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionMplsLspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.mpls.lsp.grouping.ActionMplsLsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.mpls.lsp.grouping.ActionMplsLspBuilder;

/**
 * Created by Martin Bobak mbobak@cisco.com on 10/23/14.
 */
public class MplsCodec extends AbstractActionCodec {

    private static final MplsCodec instance = new MplsCodec();
    public static final byte SUBTYPE = 5;

    /**
     * serializer key for VRF action
     */
    public static final CiscoActionSerializerKey SERIALIZER_KEY = new CiscoActionSerializerKey(
            EncodeConstants.OF13_VERSION_ID, OfjCofActionMplsLsp.class);
    /**
     * deserializer key for VRF action
     */
    public static final CiscoActionDeserializerKey DESERIALIZER_KEY = new CiscoActionDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, SUBTYPE);

    /**
     * serializer key for VRF action, OF-1.0
     */
    public static final CiscoActionSerializerKey SERIALIZER_KEY_10 = new CiscoActionSerializerKey(
            EncodeConstants.OF10_VERSION_ID, OfjCofActionMplsLsp.class);
    /**
     * deserializer key for VRF action OF-1.0
     */
    public static final CiscoActionDeserializerKey DESERIALIZER_KEY_10 = new CiscoActionDeserializerKey(
            EncodeConstants.OF10_VERSION_ID, SUBTYPE);


    @Override
    public Action deserialize(ByteBuf byteBuf) {
        ActionMplsLspBuilder actionMplsLspBuilder = new ActionMplsLspBuilder();
        ActionBuilder actionBuilder = deserializeHeader(byteBuf);
        byte[] nameBytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(nameBytes);
        actionMplsLspBuilder.setName(nameBytes);
        OfjCofActionMplsLspBuilder ofjAugCofActionBuilder = new OfjCofActionMplsLspBuilder();
        ofjAugCofActionBuilder.setActionMplsLsp(actionMplsLspBuilder.build());
        actionBuilder.setActionChoice(ofjAugCofActionBuilder.build());
        return actionBuilder.build();
    }

    /**
     * <pre>
     *  struct cof_action_mpls_lsp {
     *  ovs_be16 type;              / * OFPAT_VENDOR * /
     *  ovs_be16 len;               / * 10 + namesize * /
     *  ovs_be32 vendor;            / * CISCO_VENDOR_ID * /
     *  ovs_be16 subtype;           / * COF_AT_MPLS_LSP * /
     *  uint8_t name[0];
     *  };
     * </pre>
     *
     * @param action
     * @param byteBuf
     */

    @Override
    public void serialize(Action action, ByteBuf byteBuf) {
        ActionMplsLsp actionMplsLsp = ((OfjCofActionMplsLsp) action.getActionChoice()).getActionMplsLsp();
        int startIdx = byteBuf.writerIndex();
        serializeHeader(0, SUBTYPE, byteBuf);
        byteBuf.writeBytes(actionMplsLsp.getName());
        byteBuf.setShort(startIdx + EncodeConstants.SIZE_OF_SHORT_IN_BYTES, byteBuf.writerIndex() - startIdx);
    }

    /**
     * @return singleton
     */
    public static MplsCodec getInstance() {
        return instance;
    }

}
