/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories.multipart;

import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
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

    @Mock DeserializerRegistry registry;

    private final MultipartReplyMessageFactory factory = new MultipartReplyMessageFactory();
    @Mock
    private OFDeserializer<ExperimenterDataOfChoice> vendorDeserializer;

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyExperimenter() {
        Mockito.when(registry.getDeserializer(ArgumentMatchers.any())).thenReturn(vendorDeserializer);
        factory.injectDeserializerRegistry(registry);
        ByteBuf bb = BufferHelper.buildBuffer("FF FF 00 01 00 00 00 00 "
                                            + "00 00 00 01 00 00 00 02"); // expID, expType
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 65535, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());

        Mockito.verify(vendorDeserializer).deserialize(bb);
    }
}
