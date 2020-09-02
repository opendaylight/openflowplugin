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
import static org.mockito.Mockito.when;

import io.netty.buffer.ByteBuf;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.MultipartReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;

/**
 * Unit tests for MultipartReplyExperimenter.
 *
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipartReplyExperimenterTest {
    @Mock
    private DeserializerRegistry registry;
    @Mock
    private OFDeserializer<ExperimenterDataOfChoice> vendorDeserializer;

    private MultipartReplyMessageFactory factory;

    @Before
    public void before() {
        when(registry.getDeserializer(ArgumentMatchers.any())).thenReturn(vendorDeserializer);
        factory = new MultipartReplyMessageFactory(registry);
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyExperimenter() {
        ByteBuf bb = BufferHelper.buildBuffer("FF FF 00 01 00 00 00 00 "
                                            + "00 00 00 01 00 00 00 02"); // expID, expType
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        assertEquals("Wrong type", 65535, builtByFactory.getType().getIntValue());
        assertEquals("Wrong flag", true, builtByFactory.getFlags().isOFPMPFREQMORE());

        verify(vendorDeserializer).deserialize(bb);
    }
}
