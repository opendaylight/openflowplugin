package org.opendaylight.openflowjava.nx.codec.match;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;

public abstract class AbstractMatchCodec implements OFSerializer<MatchEntries>, OFDeserializer<MatchEntries> {

    private NxmHeader headerWithMask;
    private NxmHeader headerWithoutMask;
    
    protected MatchEntriesBuilder deserializeHeader(ByteBuf message) {
        MatchEntriesBuilder builder = new MatchEntriesBuilder();
        builder.setOxmClass(getOxmClass());
        // skip oxm_class - provided
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        builder.setOxmMatchField(getNxmField());
        boolean hasMask = (message.readUnsignedByte() & 1) != 0;
        builder.setHasMask(hasMask);
        // skip match entry length - not needed
        message.skipBytes(EncodeConstants.SIZE_OF_BYTE_IN_BYTES);
        ExperimenterIdMatchEntryBuilder experimenterIdMatchEntryBuilder = new ExperimenterIdMatchEntryBuilder();
        experimenterIdMatchEntryBuilder.setExperimenter(new ExperimenterId(NiciraConstants.NX_VENDOR_ID));
        builder.addAugmentation(ExperimenterIdMatchEntry.class, experimenterIdMatchEntryBuilder.build());
        return builder;
    }

    protected void serializeHeader(MatchEntries input, ByteBuf outBuffer) {
        outBuffer.writeInt(serializeHeaderToLong(input.isHasMask()).intValue());
    }

    private Long serializeHeaderToLong(boolean hasMask) {
        if (hasMask) {
            return getHeaderWithHasMask().toLong();
        }
        return getHeaderWithoutHasMask().toLong();
    }

    public NxmHeader getHeaderWithoutHasMask() {
        if (headerWithoutMask == null) {
            headerWithoutMask = new NxmHeader(getOxmClassCode(), getNxmFieldCode(), false, getValueLength());
        }
        return headerWithoutMask;
    }

    public NxmHeader getHeaderWithHasMask() {
        if (headerWithMask == null) {
            headerWithMask = new NxmHeader(getOxmClassCode(), getNxmFieldCode(), true, getValueLength());
        }
        return headerWithMask;
    }

    /**
     * @return numeric representation of nxm_field
     */
    public abstract int getNxmFieldCode();

    /**
     * @return numeric representation of oxm_class
     */
    public abstract int getOxmClassCode();

    /**
     * @return match entry value length
     */
    public abstract int getValueLength();

    /**
     * @return nxm_field class
     */
    public abstract Class<? extends MatchField> getNxmField();

    /**
     * @return oxm_class class
     */
    public abstract Class<? extends OxmClassBase> getOxmClass();

}
