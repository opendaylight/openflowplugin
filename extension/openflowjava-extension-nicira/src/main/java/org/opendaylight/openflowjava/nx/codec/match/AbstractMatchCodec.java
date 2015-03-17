package org.opendaylight.openflowjava.nx.codec.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;

public abstract class AbstractMatchCodec implements OFSerializer<MatchEntry>, OFDeserializer<MatchEntry> {

    private NxmHeader headerWithMask;
    private NxmHeader headerWithoutMask;

    protected MatchEntryBuilder deserializeHeader(ByteBuf message) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(getOxmClass());
        // skip oxm_class - provided
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        builder.setOxmMatchField(getNxmField());
        boolean hasMask = (message.readUnsignedByte() & 1) != 0;
        builder.setHasMask(hasMask);
        // skip match entry length - not needed
        message.skipBytes(EncodeConstants.SIZE_OF_BYTE_IN_BYTES);
        ExperimenterIdCaseBuilder experimenterIdCaseBuilder = new ExperimenterIdCaseBuilder();
        ExperimenterBuilder experimenterBuilder = new ExperimenterBuilder();
        experimenterBuilder.setExperimenter(new ExperimenterId(NiciraConstants.NX_VENDOR_ID));
        experimenterIdCaseBuilder.setExperimenter(experimenterBuilder.build());

        builder.setMatchEntryValue(experimenterIdCaseBuilder.build());
        return builder;
    }

    protected void serializeHeader(MatchEntry input, ByteBuf outBuffer) {
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
