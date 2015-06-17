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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofAtOutputNh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionOutputNh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionOutputNhBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.output.nh.grouping.ActionOutputNh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.output.nh.grouping.ActionOutputNhBuilder;

/**
 * set vrf action codec
 * <pre>
 * enum cof_nh_addrtype {
 *     COF_NH_ADDRTYPE_NONE,       /* illegal * /
 *     COF_NH_ADDRTYPE_P2P,        /* no address, use with COF_NH_ADDREXTRA_PORT * /
 *     COF_NH_ADDRTYPE_IPV4,       /* ipv4 address * /
 *     COF_NH_ADDRTYPE_IPV6,       /* ipv6 address * /
 *     COF_NH_ADDRTYPE_MAC48,      /* 48bit mac address * /
 * };
 *
 * enum cof_nh_addrextra {
 *     COF_NH_ADDREXTRA_NONE,      /* address only * /
 *     COF_NH_ADDREXTRA_PORT,      /* address and port * /
 * };
 *
 * struct cof_action_output_nh {
 *     ovs_be16 type;              /* OFPAT_VENDOR * /
 *     ovs_be16 len;               /* 16 (p2p), 24 (ipv4), 32 (mac48), 40 (ipv6)* /* /
 *     ovs_be32 vendor;            /* CISCO_VENDOR_ID * /
 *     ovs_be16 subtype;           /* COF_AT_OUTPUT_NH * /
 *     uint8_t addrtype;           /* COF_NH_ADDRTYPE_XXX * /
 *     uint8_t addrextratype;      /* COF_NH_ADDREXTRA_XXX * /
 *     union {
 *         ovs_be32 port;
 *         uint8_t pad[8];
 *     } addrextra;
 *     union {
 *         ovs_be32 ipv4;
 *         uint8_t  ipv6[16];
 *         uint8_t  mac48[6];
 *     } addr;
 *     uint8_t pad[4];
 * };
 * </pre>
 */
public class NextHopCodec extends AbstractActionCodec {

    /**
     * TODO: get enum value for wire protocol - COF_AT_VRF
     */
    public static final byte SUBTYPE = 1;
    /**
     * rfc 2685 - 7bytes
     */
    public static final int VPN_ID_LEN = 7;
    /**
     * serializer key for VRF action
     */
    public static final CiscoActionSerializerKey SERIALIZER_KEY = new CiscoActionSerializerKey(
            EncodeConstants.OF13_VERSION_ID, CofAtOutputNh.class);
    /**
     * deserializer key for VRF action
     */
    public static final CiscoActionDeserializerKey DESERIALIZER_KEY = new CiscoActionDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, SUBTYPE);

    /**
     * serializer key for VRF action, OF-1.0
     */
    public static final CiscoActionSerializerKey SERIALIZER_KEY_10 = new CiscoActionSerializerKey(
            EncodeConstants.OF10_VERSION_ID, CofAtOutputNh.class);
    /**
     * deserializer key for VRF action, OF-1.0
     */
    public static final CiscoActionDeserializerKey DESERIALIZER_KEY_10 = new CiscoActionDeserializerKey(
            EncodeConstants.OF10_VERSION_ID, SUBTYPE);

    private static final NextHopCodec instance = new NextHopCodec();

    /**
     * @return singleton
     */
    public static NextHopCodec getInstance() {
        return instance;
    }

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionOutputNh actionOutputNh = ((OfjCofActionOutputNh) input.getActionChoice()).getActionOutputNh();
        int startIdx = outBuffer.writerIndex();
        // length depends on data
        serializeHeader(0, SUBTYPE, outBuffer);
        outBuffer.writeByte(actionOutputNh.getAddressType());
        outBuffer.writeByte(actionOutputNh.getAddressExtraType());

        switch (actionOutputNh.getAddressExtraType()) {
            case 0: // NONE
                outBuffer.writeZero(8);
                break;
            case 1: // PORT
                outBuffer.writeInt(actionOutputNh.getAddressExtra().intValue());
                if (actionOutputNh.getAddressType() != 0
                        && actionOutputNh.getAddressType() != 1) {
                    outBuffer.writeZero(4);
                }
                break;
            default:
                throw new IllegalArgumentException("not implemented: " + actionOutputNh.getAddressExtraType());
        }

        switch (actionOutputNh.getAddressType()) {
            case 0: // NONE
                // TODO: throw illegal exception?
                break;
            case 1: // P2P
                // NOOP
                break;
            case 2: // IPV4
                byte[] ipv4 = actionOutputNh.getAddress();
                outBuffer.writeBytes(ipv4, 0, 4);
                break;
            case 3: // IPV6
                byte[] ipv6 = actionOutputNh.getAddress();
                outBuffer.writeBytes(ipv6, 0, 16);
                break;
            case 4: // MAC48
                byte[] mac48 = actionOutputNh.getAddress();
                outBuffer.writeBytes(mac48, 0, 6);
                break;
            default:
                throw new IllegalArgumentException("not implemented: " + actionOutputNh.getAddressExtraType());
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
        ActionOutputNhBuilder actionOutNhBuilder = new ActionOutputNhBuilder();
        actionOutNhBuilder.setAddressType((int) message.readUnsignedByte());
        actionOutNhBuilder.setAddressExtraType((int) message.readUnsignedByte());

        switch (actionOutNhBuilder.getAddressExtraType()) {
            case 0: // NONE
                message.skipBytes(8);
                break;
            case 1: // PORT
                actionOutNhBuilder.setAddressExtra(message.readUnsignedInt());
                if (actionOutNhBuilder.getAddressType() != 0
                        && actionOutNhBuilder.getAddressType() != 1) {
                    message.skipBytes(4);
                }
                break;
            default:
                // NOOP
        }

        byte[] nhAddress = null;
        switch (actionOutNhBuilder.getAddressType()) {
            case 0: // NONE
                // TODO; skip bytes if padding used
                break;
            case 1: // P2P
                // TODO; skip bytes if padding used
                break;
            case 2: // IPV4
                nhAddress = new byte[4];
                message.readBytes(nhAddress);
                break;
            case 3: // IPV6
                nhAddress = new byte[16];
                message.readBytes(nhAddress);
                break;
            case 4: // MAC48
                nhAddress = new byte[6];
                message.readBytes(nhAddress);
                break;
            default:
                // NOOP
        }
        actionOutNhBuilder.setAddress(nhAddress);

        // finish action reading (in case there is padding)
        CodecUtil.finishElementReading(message, startIdx, length);

        OfjCofActionOutputNhBuilder ofjAugCofActionBuilder = new OfjCofActionOutputNhBuilder();
        ofjAugCofActionBuilder.setActionOutputNh(actionOutNhBuilder.build());
        actionBuilder.setActionChoice(ofjAugCofActionBuilder.build());
        return actionBuilder.build();
    }

}
