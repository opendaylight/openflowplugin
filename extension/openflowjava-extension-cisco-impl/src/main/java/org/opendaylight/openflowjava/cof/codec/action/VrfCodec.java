package org.opendaylight.openflowjava.cof.codec.action;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.cof.api.CiscoActionDeserializerKey;
import org.opendaylight.openflowjava.cof.api.CiscoActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofAtVrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofAtVrfType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.VrfExtra;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.VrfName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.VrfVpnId;
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

    /** TODO: get enum value for wire protocol - COF_AT_VRF */
    public static final byte SUBTYPE = 42;
    /** rfc 2685 - 7bytes */
    public static final int VPN_ID_LEN = 7;
    /** serializer key for VRF action */
    public static final CiscoActionSerializerKey SERIALIZER_KEY = new CiscoActionSerializerKey(
            EncodeConstants.OF13_VERSION_ID, CofAtVrf.class);
    /** deserializer key for VRF action */
    public static final CiscoActionDeserializerKey DESERIALIZER_KEY = new CiscoActionDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, SUBTYPE);
    
    private static final VrfCodec instance = new VrfCodec();
    
    /**
     * @return singleton
     */
    public static VrfCodec getInstance() {
        return instance;
    }
    
    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionVrf actionVrf = input.getAugmentation(OfjAugCofAction.class).getActionVrf();
        int startIdx = outBuffer.writerIndex();
        // length depends on data
        serializeHeader(0, SUBTYPE, outBuffer);
        outBuffer.writeByte(actionVrf.getVpnType().getIntValue());
        switch (actionVrf.getVpnType()) {
        case VPNID:
            outBuffer.writeBytes(actionVrf.getVrfExtra().getVrfVpnId().getValue(), 0, VPN_ID_LEN);
            break;
        case NAME:
            outBuffer.writeBytes(actionVrf.getVrfExtra().getVrfName().getValue().getBytes());
            break;
        default:
            throw new IllegalArgumentException("invalif vrf type: "+actionVrf.getVpnType());
        }
        // fix dynamic length
        outBuffer.setShort(startIdx + EncodeConstants.SIZE_OF_SHORT_IN_BYTES, outBuffer.writerIndex() - startIdx);
    }

    @Override
    public Action deserialize(ByteBuf message) {
        int startIdx = message.readerIndex();
        int length = message.getUnsignedShort(startIdx + EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionVrfBuilder actionVrfBuilder = new ActionVrfBuilder();
        actionVrfBuilder.setVpnType(CofAtVrfType.forValue(message.readUnsignedByte()));
        VrfExtra vrfExtra = null; 
        switch (actionVrfBuilder.getVpnType()) {
        case VPNID:
            byte[] vpnIdRaw = new byte[VPN_ID_LEN];
            message.readBytes(vpnIdRaw);
            vrfExtra = new VrfExtra(new VrfVpnId(vpnIdRaw));
            break;
        case NAME:
            int currentOffset = message.readerIndex() - startIdx;
            byte[] vpnNameRaw = new byte[length - currentOffset];
            message.readBytes(vpnNameRaw);
            vrfExtra = new VrfExtra(new VrfName(new String(vpnNameRaw)));
            break;
        default:
            // NOOP, device sent invalid vrfExtra value
        }
        actionVrfBuilder.setVrfExtra(vrfExtra);
        
        OfjAugCofActionBuilder ofjAugCofActionBuilder = new OfjAugCofActionBuilder();
        ofjAugCofActionBuilder.setActionVrf(actionVrfBuilder.build());
        actionBuilder.addAugmentation(ExperimenterIdAction.class, createExperimenterIdAction(CofAtVrf.class));
        actionBuilder.addAugmentation(OfjAugCofAction.class, ofjAugCofActionBuilder.build());
        return actionBuilder.build();
    }

}
