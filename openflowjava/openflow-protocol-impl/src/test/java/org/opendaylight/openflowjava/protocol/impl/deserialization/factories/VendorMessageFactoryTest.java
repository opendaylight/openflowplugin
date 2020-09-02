/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.util.ByteBufUtils;

/**
 * Unit tests for VendorMessageFactory.
 *
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.class)
public class VendorMessageFactoryTest {
    @Mock
    private DeserializerRegistry registry;
    @Mock
    private ExperimenterMessageFactory deserializer;

    /**
     * Tests {@link VendorMessageFactory#deserialize(ByteBuf)}.
     */
    @Test
    public void test() {
        Mockito.when(registry.getDeserializer(ArgumentMatchers.any())).thenReturn(deserializer);
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf("01 02 03 04 FF FF FF FF 80 00 00 00");
        VendorMessageFactory factory = new VendorMessageFactory(registry);
        factory.deserialize(buffer);

        Mockito.verify(deserializer, Mockito.times(1)).deserialize(buffer);
        Assert.assertEquals("Buffer index modified", 4, buffer.readableBytes());
    }
}
