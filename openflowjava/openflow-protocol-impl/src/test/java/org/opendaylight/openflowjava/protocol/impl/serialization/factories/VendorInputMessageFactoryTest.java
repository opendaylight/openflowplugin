/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;

/**
 * Unit tests for VendorInputMessageFactory.
 *
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.class)
public class VendorInputMessageFactoryTest {

    @Mock SerializerRegistry registry;
    @Mock OFSerializer<ExperimenterDataOfChoice> foundSerializer;
    @Mock ExperimenterDataOfChoice vendorData;
    VendorInputMessageFactory serializer;

    /**
     * Tests {@link VendorInputMessageFactory#serialize(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow
     * .protocol.rev130731.ExperimenterOfMessage, ByteBuf)}.
     */
    @Test
    public void test() {
        Mockito.when(registry.getSerializer(ArgumentMatchers.<MessageTypeKey<?>>any()))
            .thenReturn(serializer);
        final VendorInputMessageFactory factory = new VendorInputMessageFactory(registry);
        final ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        ExperimenterInputBuilder builder = new ExperimenterInputBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.setXid(12345L);
        builder.setExperimenter(new ExperimenterId(42L));
        builder.setExpType(84L);
        builder.setExperimenterDataOfChoice(vendorData);
        ExperimenterInput experimenterInput = builder.build();

        Mockito.when(registry.getSerializer(
                ArgumentMatchers.<ExperimenterIdSerializerKey<ExperimenterDataOfChoice>>any()))
                .thenReturn(foundSerializer);
        factory.serialize(experimenterInput, buffer);
        Mockito.verify(foundSerializer, Mockito.times(1))
            .serialize(experimenterInput.getExperimenterDataOfChoice(), buffer);
    }
}
