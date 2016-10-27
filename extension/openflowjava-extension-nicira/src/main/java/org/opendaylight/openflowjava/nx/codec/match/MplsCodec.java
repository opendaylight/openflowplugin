package org.opendaylight.openflowjava.nx.codec.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfMplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.mpls.label.grouping.MplsLabelValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.OfMplsCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.OfMplsCaseValueBuilder;

public class MplsCodec extends AbstractMatchCodec {

  private static final int VALUE_LENGTH = 4;
  private static final int NXM_FIELD_CODE = 34;

  public static final MatchEntrySerializerKey<OxmClassBase, NxmOfMplsLabel> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
      EncodeConstants.OF13_VERSION_ID, OxmClassBase.class,
      NxmOfMplsLabel.class);

  public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
      EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.OPENFLOW_BASIC_CLASS,
      NXM_FIELD_CODE);

  @Override
  public void serialize(MatchEntry input, ByteBuf outBuffer) {
    serializeHeader(input, outBuffer);
    OfMplsCaseValue value = (OfMplsCaseValue) input.getMatchEntryValue();
    outBuffer.writeInt(value.getMplsLabelValues().getMplsLabel().intValue());
  }

  @Override
  public MatchEntry deserialize(ByteBuf message) {
    MatchEntryBuilder matchEntryBuilder = deserializeHeader(message);
    OfMplsCaseValueBuilder caseBuilder = new OfMplsCaseValueBuilder();
    MplsLabelValuesBuilder valuesBuilder = new MplsLabelValuesBuilder();
    valuesBuilder.setMplsLabel(message.readLong()).build();
    caseBuilder.setMplsLabelValues(valuesBuilder.build());
    matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
    return matchEntryBuilder.build();
  }

  @Override
  public int getNxmFieldCode() {
    return NXM_FIELD_CODE;
  }

  @Override
  public int getOxmClassCode() {
    return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
  }

  @Override
  public int getValueLength() {
    return VALUE_LENGTH;
  }

  @Override
  public Class<? extends MatchField> getNxmField() {
    return NxmOfMplsLabel.class;
  }

  @Override
  public Class<? extends OxmClassBase> getOxmClass() {
    return OxmClassBase.class;
  }
}
