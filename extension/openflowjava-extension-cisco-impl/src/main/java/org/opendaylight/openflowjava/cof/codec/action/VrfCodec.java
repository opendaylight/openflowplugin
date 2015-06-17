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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofAtVrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionVrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionVrfBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.vrf.grouping.ActionVrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.vrf.grouping.ActionVrfBuilder;

/**
 * set vrf action codec
 * <pre>
 * enum cof_vrftype {
 *     COF_VRFTYPE_NONE,  /* illegal value * /
 *     COF_VRFTYPE_VPNID, /* vpn-id (rfc 2685) * /
 *     COF_VRFTYPE_NAME,  /* vrf namestring (sized determine by len * /
 * };
 *
 * struct cof_action_vrf {
 *     ovs_be16 type;         /* OFPAT_VENDOR * /
 *     ovs_be16 len;          /* 18 for vpn_id (11 + namesize) for name * /
 *     ovs_be32 vendor;       /* CISCO_VENDOR_ID * /
 *     ovs_be16 subtype;      /* COF_AT_VRF * /
 *     uint8_t vpntype;       /* COF_VRFTYPE_XXX * /
 *     union {
 *         uint8_t vpn_id[7];
 *         uint8_t vrf_name[0];
 *      } vrfextra;
 * };
 * </pre>
 */
public class VrfCodec extends AbstractActionCodec {

    /**
     * TODO: get enum value for wire protocol - COF_AT_VRF
     */
    public static final byte SUBTYPE = 4;
    /**
     * rfc 2685 - 7bytes
     */
    public static final int VPN_ID_LEN = 7;
    /**
     * serializer key for VRF action
     */
    public static final CiscoActionSerializerKey SERIALIZER_KEY = new CiscoActionSerializerKey(
            EncodeConstants.OF13_VERSION_ID, OfjCofActionVrf.class);
    /**
     * deserializer key for VRF action
     */
    public static final CiscoActionDeserializerKey DESERIALIZER_KEY = new CiscoActionDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, SUBTYPE);

    /**
     * serializer key for VRF action, OF-1.0
     */
    public static final CiscoActionSerializerKey SERIALIZER_KEY_10 = new CiscoActionSerializerKey(
            EncodeConstants.OF10_VERSION_ID, OfjCofActionVrf.class);
    /**
     * deserializer key for VRF action OF-1.0
     */
    public static final CiscoActionDeserializerKey DESERIALIZER_KEY_10 = new CiscoActionDeserializerKey(
            EncodeConstants.OF10_VERSION_ID, SUBTYPE);


    private static final VrfCodec instance = new VrfCodec();

    /**
     * @return singleton
     */
    public static VrfCodec getInstance() {
        return instance;
    }

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionVrf actionVrf = ((OfjCofActionVrf) input.getActionChoice()).getActionVrf();
        int startIdx = outBuffer.writerIndex();
        // length depends on data
        serializeHeader(0, SUBTYPE, outBuffer);
        outBuffer.writeByte(actionVrf.getVpnType());
        switch (actionVrf.getVpnType()) {
            case 1: // VPNID
                outBuffer.writeBytes(actionVrf.getVrfExtra(), 0, VPN_ID_LEN);
                break;
            case 2: // NAME
                outBuffer.writeBytes(actionVrf.getVrfExtra());
                break;
            default:
                throw new IllegalArgumentException("invalif vrf type: " + actionVrf.getVpnType());
        }

        // add padding if needed
        outBuffer.writeZero(CodecUtil.computePadding8(outBuffer.writerIndex() - startIdx));

        // fix dynamic length
        outBuffer.setShort(startIdx + EncodeConstants.SIZE_OF_SHORT_IN_BYTES, outBuffer.writerIndex() - startIdx);
    }

    @Override
    public Action deserialize(ByteBuf message) {
        int startIdx = message.readerIndex();
        int length = CodecUtil.getCofActionLength(message, startIdx);
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionVrfBuilder actionVrfBuilder = new ActionVrfBuilder();
        actionVrfBuilder.setVpnType((int) message.readUnsignedByte());
        byte[] vrfExtra = null;
        switch (actionVrfBuilder.getVpnType()) {
            case 1: // VPNID
                vrfExtra = new byte[VPN_ID_LEN];
                message.readBytes(vrfExtra);
                break;
            case 2: // NAME
                int currentOffset = message.readerIndex() - startIdx;
                vrfExtra = new byte[length - currentOffset];
                message.readBytes(vrfExtra);
                vrfExtra = CodecUtil.stripTrailingZeroes(vrfExtra);
                break;
            default:
                // NOOP, device sent invalid vrfExtra value
        }
        actionVrfBuilder.setVrfExtra(vrfExtra);

        // finish action reading (in case there is padding)
        CodecUtil.finishElementReading(message, startIdx, length);

        OfjCofActionVrfBuilder ofjAugCofActionBuilder = new OfjCofActionVrfBuilder();
        ofjAugCofActionBuilder.setActionVrf(actionVrfBuilder.build());
        actionBuilder.setActionChoice(ofjAugCofActionBuilder.build());
        return actionBuilder.build();
    }

}
