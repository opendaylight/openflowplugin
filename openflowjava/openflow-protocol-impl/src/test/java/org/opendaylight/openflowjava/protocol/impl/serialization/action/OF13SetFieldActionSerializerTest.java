/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.action;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.match.entry.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.match.entry.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for OF13SetFieldActionSerializer.
 *
 * @author madamjak
 */
@RunWith(MockitoJUnitRunner.class)
public class OF13SetFieldActionSerializerTest {

    private SerializerRegistry registry;
    @Mock OFSerializer<MatchEntry> serializerMock;

    /**
     * Initialize registry and mock.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
    }

    /**
     * Test identify ExperimenterClass serializer.
     */
    @Test
    public void test() {
        OF13SetFieldActionSerializer ser = new OF13SetFieldActionSerializer();
        ser.injectSerializerRegistry(registry);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping
            .ActionBuilder actionBuilder = new ActionBuilder();
        final Uint32 experimenterId = Uint32.valueOf(12L);
        ExperimenterIdCaseBuilder expCaseBuilder = new ExperimenterIdCaseBuilder();
        ExperimenterBuilder expBuilder = new ExperimenterBuilder();
        expBuilder.setExperimenter(new ExperimenterId(experimenterId));
        expCaseBuilder.setExperimenter(expBuilder.build());
        MatchEntryBuilder meb = new MatchEntryBuilder();
        meb.setOxmClass(ExperimenterClass.VALUE);
        meb.setOxmMatchField(OxmMatchFieldClass.VALUE);
        meb.setMatchEntryValue(expCaseBuilder.build());
        List<MatchEntry> matchEntry = new ArrayList<>();
        MatchEntry me = meb.build();
        matchEntry.add(me);
        SetFieldCaseBuilder caseBuilder = new SetFieldCaseBuilder();
        SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();
        setFieldBuilder.setMatchEntry(matchEntry);
        caseBuilder.setSetFieldAction(setFieldBuilder.build());
        actionBuilder.setActionChoice(caseBuilder.build());
        MatchEntrySerializerKey<?, ?> key = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3, ExperimenterClass.VALUE, OxmMatchFieldClass.VALUE);
        key.setExperimenterId(experimenterId);
        registry.registerSerializer(key, serializerMock);
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        ser.serialize(actionBuilder.build(), out);
        Mockito.verify(serializerMock, Mockito.times(1)).serialize(Mockito.any(), Mockito.any());
        int lenght = out.readableBytes();
        Assert.assertEquals("Wrong - bad field code", ActionConstants.SET_FIELD_CODE, out.readUnsignedShort());
        Assert.assertEquals("Wrong - bad lenght", lenght, out.readUnsignedShort());
    }

    private interface OxmMatchFieldClass extends MatchField {
        // only for testing purposes
    }
}
