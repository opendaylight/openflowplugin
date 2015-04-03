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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxTunIpv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.tun.ipv4.dst.grouping.TunIpv4DstValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.tun.ipv4.src.grouping.TunIpv4SrcValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIpv4DstCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIpv4DstCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIpv4SrcCaseValueBuilder;

public class TunIpv4SrcCodec extends AbstractMatchCodec {

    private static final int VALUE_LENGTH = 4;
    private static final int NXM_FIELD_CODE = 31;
    public static final MatchEntrySerializerKey<Nxm1Class, NxmNxTunIpv4Src> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
            EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxTunIpv4Src.class);
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
        TunIpv4SrcCaseValueBuilder caseBuilder = new TunIpv4SrcCaseValueBuilder();
        TunIpv4SrcValuesBuilder valuesBuilder = new TunIpv4SrcValuesBuilder();
        valuesBuilder.setValue(message.readUnsignedInt());
        caseBuilder.setTunIpv4SrcValues(valuesBuilder.build());
        matchEntriesBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntriesBuilder.build();
    }

//    @Override
//    public void serialize(MatchEntries input, ByteBuf outBuffer) {
//        serializeHeader(input, outBuffer);
//        Long value = input.getAugmentation(OfjAugNxMatch.class).getTunIpv4SrcValues().getValue();
//        outBuffer.writeInt(value.intValue());
//    }
//
//    @Override
//    public MatchEntries deserialize(ByteBuf message) {
//        MatchEntriesBuilder matchEntriesBuilder = deserializeHeader(message);
//        OfjAugNxMatchBuilder augNxMatchBuilder = new OfjAugNxMatchBuilder();
//        augNxMatchBuilder.setTunIpv4SrcValues(new TunIpv4SrcValuesBuilder().setValue(message.readUnsignedInt()).build());
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
        return NxmNxTunIpv4Src.class;
    }

    @Override
    public Class<? extends OxmClassBase> getOxmClass() {
        return Nxm1Class.class;
    }

}
