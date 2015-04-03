package org.opendaylight.openflowjava.nx.codec.match;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxTunIpv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.tun.ipv4.dst.grouping.TunIpv4DstValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIpv4DstCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIpv4DstCaseValueBuilder;

public class TunIpv4DstCodec extends AbstractMatchCodec {

    private static final int VALUE_LENGTH = 4;
    private static final int NXM_FIELD_CODE = 32;
    public static final MatchEntrySerializerKey<Nxm1Class, NxmNxTunIpv4Dst> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
            EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxTunIpv4Dst.class);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, NXM_FIELD_CODE);

    @Override
    public void serialize(MatchEntry input, ByteBuf outBuffer) {
        serializeHeader(input, outBuffer);
        TunIpv4DstCaseValue caseValue = (TunIpv4DstCaseValue) input.getMatchEntryValue();
        outBuffer.writeInt(caseValue.getTunIpv4DstValues().getValue().intValue());
    }

    @Override
    public MatchEntry deserialize(ByteBuf message) {
        MatchEntryBuilder matchEntriesBuilder = deserializeHeader(message);
        TunIpv4DstCaseValueBuilder caseBuilder = new TunIpv4DstCaseValueBuilder();
        TunIpv4DstValuesBuilder valuesBuilder = new TunIpv4DstValuesBuilder();
        valuesBuilder.setValue(message.readUnsignedInt());
        caseBuilder.setTunIpv4DstValues(valuesBuilder.build());
        matchEntriesBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntriesBuilder.build();
    }

//    @Override
//    public void serialize(MatchEntries input, ByteBuf outBuffer) {
//        serializeHeader(input, outBuffer);
//        Long value = input.getAugmentation(OfjAugNxMatch.class).getTunIpv4DstValues().getValue();
//        outBuffer.writeInt(value.intValue());
//    }
//
//    @Override
//    public MatchEntries deserialize(ByteBuf message) {
//        MatchEntriesBuilder matchEntriesBuilder = deserializeHeader(message);
//        OfjAugNxMatchBuilder augNxMatchBuilder = new OfjAugNxMatchBuilder();
//        augNxMatchBuilder.setTunIpv4DstValues(new TunIpv4DstValuesBuilder().setValue(message.readUnsignedInt()).build());
//        matchEntriesBuilder.addAugmentation(OfjAugNxMatch.class, augNxMatchBuilder.build());
//        return matchEntriesBuilder.build();
//    }

    @Override
    public int getNxmFieldCode() {
        return NXM_FIELD_CODE;
    }

    @Override
    public int getOxmClassCode() {
        return OxmMatchConstants.NXM_1_CLASS;
    }

    @Override
    public int getValueLength() {
        return VALUE_LENGTH;
    }

    @Override
    public Class<? extends MatchField> getNxmField() {
        return NxmNxTunIpv4Dst.class;
    }

    @Override
    public Class<? extends OxmClassBase> getOxmClass() {
        return Nxm1Class.class;
    }

}
