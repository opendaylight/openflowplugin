package org.opendaylight.openflowjava.cof.codec.action;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.cof.api.CiscoActionDeserializerKey;
import org.opendaylight.openflowjava.cof.api.CiscoActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofAtOutputNh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofAtOutputNhAddressExtraType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofAtOutputNhAddressType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.NhPortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.output.nh.grouping.ActionOutputNh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.output.nh.grouping.ActionOutputNh.Address;
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

    /** TODO: get enum value for wire protocol - COF_AT_VRF */
    public static final byte SUBTYPE = 43;
    /** rfc 2685 - 7bytes */
    public static final int VPN_ID_LEN = 7;
    /** serializer key for VRF action */
    public static final CiscoActionSerializerKey SERIALIZER_KEY = new CiscoActionSerializerKey(
            EncodeConstants.OF13_VERSION_ID, CofAtOutputNh.class);
    /** deserializer key for VRF action */
    public static final CiscoActionDeserializerKey DESERIALIZER_KEY = new CiscoActionDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, SUBTYPE);
    
    private static final NextHopCodec instance = new NextHopCodec();
    
    /**
     * @return singleton
     */
    public static NextHopCodec getInstance() {
        return instance;
    }
    
    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionOutputNh actionOutputNh = input.getAugmentation(OfjAugCofAction.class).getActionOutputNh();
        int startIdx = outBuffer.writerIndex();
        // length depends on data
        serializeHeader(0, SUBTYPE, outBuffer);
        outBuffer.writeByte(actionOutputNh.getAddressType().getIntValue());
        outBuffer.writeByte(actionOutputNh.getAddressExtraType().getIntValue());
        
        switch (actionOutputNh.getAddressExtraType()) {
        case PORT:
            outBuffer.writeInt(actionOutputNh.getAddressExtra().getValue().intValue());
            break;
        case NONE:
            // TODO: write padding? 
            break;
        default:
            throw new IllegalArgumentException("not implemented: "+actionOutputNh.getAddressExtraType());
        }
        
        switch (actionOutputNh.getAddressType()) {
        case IPV4:
            String ipv4 = actionOutputNh.getAddress().getIpv4Address().getValue();
            Iterable<String> address4Groups = ByteBufUtils.DOT_SPLITTER.split(ipv4);
            for (String group : address4Groups) {
                outBuffer.writeByte(Short.parseShort(group));
            }
            break;
        case IPV6:
            String ipv6 = actionOutputNh.getAddress().getIpv6Address().getValue();
            Iterable<String> address6Groups = ByteBufUtils.COLON_SPLITTER.split(ipv6);
            for (String group : address6Groups) {
                outBuffer.writeShort(Integer.parseInt(group, 16));
            }
            break;
        case MAC48:
            String mac48 = actionOutputNh.getAddress().getMacAddress().getValue();
            outBuffer.writeBytes(ByteBufUtils.macAddressToBytes(mac48));
            break;
        case P2P:
            // NOOP, TODO: write padding?
            break;
        case NONE:
            // TODO: write padding? 
            break;
        default:
            throw new IllegalArgumentException("not implemented: "+actionOutputNh.getAddressExtraType());
        }
        
        // fix dynamic length
        outBuffer.setShort(startIdx + EncodeConstants.SIZE_OF_SHORT_IN_BYTES, outBuffer.writerIndex() - startIdx);
    }

    @Override
    public Action deserialize(ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionOutputNhBuilder actionOutNhBuilder = new ActionOutputNhBuilder();
        actionOutNhBuilder.setAddressType(CofAtOutputNhAddressType.forValue(message.readUnsignedByte()));
        actionOutNhBuilder.setAddressExtraType(CofAtOutputNhAddressExtraType.forValue(message.readUnsignedByte()));
        
        switch (actionOutNhBuilder.getAddressExtraType()) {
        case PORT:
            actionOutNhBuilder.setAddressExtra(new NhPortNumber(message.readUnsignedInt()));
            break;
        case NONE:
            // TODO; skip bytes if padding used
            break;
        default:
            // NOOP
        }
        
        Address nhAddress = null;
        switch (actionOutNhBuilder.getAddressType()) {
        case IPV4:
            nhAddress = new Address(new Ipv4Address(ByteBufUtils.readIpv4Address(message)));
            break;
        case IPV6:
            nhAddress = new Address(new Ipv6Address(ByteBufUtils.readIpv6Address(message)));
            break;
        case MAC48:
            byte[] mac48Raw = new byte[6];
            message.readBytes(mac48Raw);
            nhAddress = new Address(new MacAddress(ByteBufUtils.macAddressToString(mac48Raw)));
            break;
        case P2P:
            // TODO; skip bytes if padding used
            break;
        case NONE:
            // TODO; skip bytes if padding used
            break;
        default:
            // NOOP
        }
        actionOutNhBuilder.setAddress(nhAddress);
        
        OfjAugCofActionBuilder ofjAugCofActionBuilder = new OfjAugCofActionBuilder();
        ofjAugCofActionBuilder.setActionOutputNh(actionOutNhBuilder.build());
        actionBuilder.addAugmentation(ExperimenterIdAction.class, createExperimenterIdAction(CofAtOutputNh.class));
        actionBuilder.addAugmentation(OfjAugCofAction.class, ofjAugCofActionBuilder.build());
        return actionBuilder.build();
    }

}
