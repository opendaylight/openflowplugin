/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.core.connection.UdpMessageListenerWrapper;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for OFDatagramPacketEncoder.
 *
 * @author michal.polkorab
 */
public class OFDatagramPacketEncoderTest {

    @Mock ChannelHandlerContext ctx;
    @Mock GenericFutureListener<Future<Void>> listener;
    @Mock SerializationFactory factory;

    private UdpMessageListenerWrapper wrapper;
    private final InetSocketAddress address = new InetSocketAddress("10.0.0.1", 6653);
    private List<Object> out;

    /**
     * Initializes mocks and other objects.
     *
     * @param version openflow protocol wire version
     */
    public void startUp(Uint8 version) {
        MockitoAnnotations.initMocks(this);
        out = new ArrayList<>();
        HelloInputBuilder builder = new HelloInputBuilder();
        builder.setVersion(version);
        HelloInput hello = builder.build();
        wrapper = new UdpMessageListenerWrapper(hello, listener, address);
    }

    /**
     * Tests encoding.
     */
    @Test
    public void testCorrectEncode() throws Exception {
        startUp(EncodeConstants.OF_VERSION_1_3);
        OFDatagramPacketEncoder encoder = new OFDatagramPacketEncoder();
        encoder.setSerializationFactory(factory);
        encoder.encode(ctx, wrapper, out);
    }

    /**
     * Tests encoding.
     */
    @Test
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void testIncorrectEncode() {
        startUp(null);
        OFDatagramPacketEncoder encoder = new OFDatagramPacketEncoder();
        encoder.setSerializationFactory(factory);
        try {
            encoder.encode(ctx, wrapper, out);
        } catch (Exception e) {
            verify(wrapper, times(1)).getListener();
            Assert.assertEquals("List should be empty", 0, out.size());
        }
    }
}
