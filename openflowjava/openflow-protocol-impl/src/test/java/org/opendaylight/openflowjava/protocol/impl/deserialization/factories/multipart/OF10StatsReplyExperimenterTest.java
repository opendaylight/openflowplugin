/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories.multipart;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import io.netty.buffer.ByteBuf;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10StatsReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;

/**
 * Unit tests for OF10StatsReplyExperimenter.
 *
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.class)
public class OF10StatsReplyExperimenterTest {
    @Mock
    private DeserializerRegistry registry;
    @Mock
    private OFDeserializer<ExperimenterDataOfChoice> vendorDeserializer;

    /**
     * Tests {@link OF10StatsReplyMessageFactory} for experimenter body translation.
     */
    @Test
    public void test() {
        Mockito.when(registry.getDeserializer(ArgumentMatchers.any())).thenReturn(vendorDeserializer);
        OF10StatsReplyMessageFactory factory = new OF10StatsReplyMessageFactory(registry);

        ByteBuf bb = BufferHelper.buildBuffer("FF FF 00 01 00 00 00 00 "
                                            + "00 00 00 01"); // expID
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        assertEquals("Wrong type", 65535, builtByFactory.getType().getIntValue());
        assertEquals("Wrong flag", true, builtByFactory.getFlags().isOFPMPFREQMORE());

        verify(vendorDeserializer).deserialize(bb);
    }
}
