/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.netty.buffer.ByteBuf;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;

/**
 * Unit tests for ExperimenterMessageFactory.
 *
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.class)
public class ExperimenterMessageFactoryTest {

    @Mock DeserializerRegistry registry;
    @Mock OFDeserializer<ExperimenterDataOfChoice> deserializer;
    @Mock ExperimenterDataOfChoice message;
    private ExperimenterMessageFactory factory;

    /**
     * Initializes mocks.
     */
    @Before
    public void startUp() {
        factory = new ExperimenterMessageFactory(registry);
    }

    /**
     * Test deserializer lookup correctness.
     */
    @Test
    public void test() {
        when(registry.getDeserializer(any(ExperimenterIdDeserializerKey.class))).thenReturn(deserializer);
        when(deserializer.deserialize(any(ByteBuf.class))).thenReturn(message);

        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf("00 01 02 03 00 00 00 10 00 00 00 20");
        ExperimenterMessage deserializedMessage = factory.deserialize(buffer);
        assertEquals("Wrong return value", message, deserializedMessage.getExperimenterDataOfChoice());
        assertEquals("ByteBuf index moved", 0, buffer.readableBytes());
    }
}
