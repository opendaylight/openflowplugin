package org.opendaylight.openflowjava.nx.codec.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.src._case.EthSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfEthSrc;

public class EthSrcCodec extends AbstractMatchCodec {

    private static final int VALUE_LENGTH = 6;
    private static final int NXM_FIELD_CODE = 2;
    public static final MatchEntrySerializerKey<Nxm0Class, NxmOfEthSrc> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
            EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfEthSrc.class);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, NXM_FIELD_CODE);

    @Override
    public void serialize(MatchEntry input, ByteBuf outBuffer) {
        serializeHeader(input, outBuffer);
        EthSrcCase ethSrcCase = ((EthSrcCase) input.getMatchEntryValue());
        outBuffer.writeBytes(ByteBufUtils.macAddressToBytes(ethSrcCase.getEthSrc().getMacAddress().getValue()));
    }

    @Override
    public MatchEntry deserialize(ByteBuf message) {
        MatchEntryBuilder matchEntryBuilder = deserializeHeader(message);
        byte[] address = new byte[VALUE_LENGTH];
        message.readBytes(address);
        EthSrcCaseBuilder ethSrcCaseBuilder = new EthSrcCaseBuilder();
        EthSrcBuilder ethSrcBuilder = new EthSrcBuilder();
        ethSrcBuilder.setMacAddress(new MacAddress(ByteBufUtils.macAddressToString(address)));
        ethSrcCaseBuilder.setEthSrc(ethSrcBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ethSrcCaseBuilder.build());
        matchEntryBuilder.setHasMask(false);
        return matchEntryBuilder.build();
    }

    @Override
    public int getNxmFieldCode() {
        return NXM_FIELD_CODE;
    }

    @Override
    public int getOxmClassCode() {
        return OxmMatchConstants.NXM_0_CLASS;
    }

    @Override
    public int getValueLength() {
        return VALUE_LENGTH;
    }

    @Override
    public Class<? extends MatchField> getNxmField() {
        return NxmOfEthSrc.class;
    }

    @Override
    public Class<? extends OxmClassBase> getOxmClass() {
        return Nxm0Class.class;
    }

}
