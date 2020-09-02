/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import static org.mockito.ArgumentMatchers.any;

import io.netty.buffer.ByteBuf;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterOfMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;

/**
 * Unit tests for ExperimenterInputMessageFactory.
 *
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ExperimenterInputMessageFactoryTest {
    @Mock
    private SerializerRegistry registry;
    @Mock
    private OFSerializer<ExperimenterDataOfChoice> serializer;
    private OFSerializer<ExperimenterOfMessage> expFactory;
    @Mock
    private ExperimenterDataOfChoice vendorData;
    @Mock
    private ByteBuf out;

    /**
     * Sets up ExperimenterInputMessageFactory.
     * @param real true if setup should use real registry, false when mock is desired
     */
    public void startUp(final boolean real) {
        if (real) {
            SerializerRegistry realRegistry = new SerializerRegistryImpl();
            realRegistry.init();
            expFactory = new ExperimenterInputMessageFactory(realRegistry);
        } else {
            expFactory = new ExperimenterInputMessageFactory(registry);
        }
    }

    /**
     * Testing of {@link ExperimenterInputMessageFactory} for correct serializer
     * lookup and serialization.
     */
    @Test(expected = IllegalStateException.class)
    public void testV10Real() throws Exception {
        startUp(true);
        ExperimenterInputBuilder builder = new ExperimenterInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setExperimenter(new ExperimenterId(42L));
        builder.setExpType(21L);
        builder.setExperimenterDataOfChoice(vendorData);
        ExperimenterInput input = builder.build();

        expFactory.serialize(input, out);
    }

    /**
     * Testing of {@link ExperimenterInputMessageFactory} for correct serializer
     * lookup and serialization.
     */
    @Test(expected = IllegalStateException.class)
    public void testV13Real() throws Exception {
        startUp(true);
        ExperimenterInputBuilder builder = new ExperimenterInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setExperimenter(new ExperimenterId(42L));
        builder.setExpType(22L);
        builder.setExperimenterDataOfChoice(vendorData);
        ExperimenterInput input = builder.build();

        expFactory.serialize(input, out);
    }

    /**
     * Testing of {@link ExperimenterInputMessageFactory} for correct serializer
     * lookup and serialization.
     */
    @Test
    public void testV10() throws Exception {
        startUp(false);
        ExperimenterInputBuilder builder = new ExperimenterInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setExperimenter(new ExperimenterId(42L));
        builder.setExpType(21L);
        builder.setExperimenterDataOfChoice(vendorData);
        ExperimenterInput input = builder.build();

        Mockito.when(registry.getSerializer(
            any())).thenReturn(serializer);

        expFactory.serialize(input, out);
        Mockito.verify(serializer, Mockito.times(1)).serialize(input.getExperimenterDataOfChoice(), out);
    }

    /**
     * Testing of {@link ExperimenterInputMessageFactory} for correct serializer
     * lookup and serialization.
     */
    @Test
    public void testV13() throws Exception {
        startUp(false);
        ExperimenterInputBuilder builder = new ExperimenterInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setExperimenter(new ExperimenterId(42L));
        builder.setExpType(21L);
        builder.setExperimenterDataOfChoice(vendorData);
        ExperimenterInput input = builder.build();

        Mockito.when(registry.getSerializer(
            any())).thenReturn(serializer);

        expFactory.serialize(input, out);
        Mockito.verify(serializer, Mockito.times(1)).serialize(input.getExperimenterDataOfChoice(), out);
    }
}
