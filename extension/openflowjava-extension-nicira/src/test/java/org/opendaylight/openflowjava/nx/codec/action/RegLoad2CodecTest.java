/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load2.grouping.NxActionRegLoad2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load2.grouping.NxActionRegLoad2Builder;

@RunWith(MockitoJUnitRunner.class)
public class RegLoad2CodecTest {

    private RegLoad2Codec regLoad2Codec;

    @Mock
    private SerializerRegistry serializerRegistry;
    @Mock
    private DeserializerRegistry deserializerRegistry;
    @Mock
    private OFSerializer<MatchEntry> ofSerializer;
    @Mock
    private OFDeserializer<MatchEntry> ofDeserializer;

    @Before
    public void setup() {
        regLoad2Codec = new RegLoad2Codec(serializerRegistry, deserializerRegistry);
    }

    @Test
    public void deserialize() {
        final ByteBuf byteBuf = ByteBufUtils.hexStringToByteBuf(
                "FF FF 00 14 00 00 23 20 00 21"             // REG_LOAD2 header
                + " FF FF 02 00 00 5A D6 50"                       // OXM field
                + " 00 00 00 00 00 00");                           // padding
        MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(
                EncodeConstants.OF13_VERSION_ID,
                EncodeConstants.EXPERIMENTER_VALUE,
                1);
        key.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        final MatchEntry matchEntry = new MatchEntryBuilder().build();
        when(deserializerRegistry.getDeserializer(key)).thenReturn(ofDeserializer);
        when(ofDeserializer.deserialize(byteBuf)).thenAnswer(invocationOnMock -> {
            invocationOnMock.<ByteBuf>getArgument(0).skipBytes(8);
            return matchEntry;
        });

        Action action = regLoad2Codec.deserialize(byteBuf);

        assertEquals(0, byteBuf.readableBytes());
        assertSame(((ActionRegLoad2) action.getActionChoice()).getNxActionRegLoad2().getMatchEntry().get(0),
                matchEntry);
    }

    @Test
    public void serialize() {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        final Action action = createAction();
        MatchEntrySerializerKey<?, ?> key = new MatchEntrySerializerKey<>(
                EncodeConstants.OF13_VERSION_ID,
                ExperimenterClass.class,
                OxmMatchFieldClass.class);
        key.setExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID);
        when(serializerRegistry.getSerializer(key)).thenReturn(ofSerializer);
        doNothing().when(ofSerializer).serialize(any(), any());

        regLoad2Codec.serialize(action, out);

        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, out.readUnsignedShort());
        assertEquals(16, out.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.longValue(), out.readUnsignedInt());
        assertEquals(RegLoad2Codec.SUBTYPE, out.readUnsignedShort());
    }

    private static Action createAction() {
        ExperimenterBuilder experimenterBuilder = new ExperimenterBuilder();
        experimenterBuilder.setExperimenter(new ExperimenterId(NiciraConstants.NX_NSH_VENDOR_ID));
        ExperimenterIdCaseBuilder expCaseBuilder = new ExperimenterIdCaseBuilder();
        expCaseBuilder.setExperimenter(experimenterBuilder.build());
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmMatchField(OxmMatchFieldClass.class);
        matchEntryBuilder.setOxmClass(ExperimenterClass.class);
        matchEntryBuilder.setMatchEntryValue(expCaseBuilder.build());
        NxActionRegLoad2 nxActionRegLoad2 = new NxActionRegLoad2Builder()
                .setMatchEntry(Collections.singletonList(matchEntryBuilder.build()))
                .build();
        ActionRegLoad2 actionRegLoad2 = new ActionRegLoad2Builder().setNxActionRegLoad2(nxActionRegLoad2).build();
        return new ActionBuilder().setActionChoice(actionRegLoad2).build();
    }

    private interface OxmMatchFieldClass extends MatchField {
        // only for testing purposes
    }
}