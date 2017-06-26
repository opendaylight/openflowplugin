/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import static org.mockito.Mockito.when;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import java.net.InetSocketAddress;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
/**
 *
 * @author madamjak
 *
 */
public class ConnectionAdapterFactoryImplTest {

    @Mock ChannelPipeline channnelPipe;
    @Mock Channel channel;
    @Mock InetSocketAddress address;

    @Before
    public void startUp(){
        MockitoAnnotations.initMocks(this);
        when(channel.pipeline()).thenReturn(channnelPipe);
    }

    @Test
    public void test(){
        final ConnectionAdapterFactoryImpl connAdapterFactory = new ConnectionAdapterFactoryImpl();
        final ConnectionFacade connFacade = connAdapterFactory.createConnectionFacade(channel, address, true);
        Assert.assertNotNull("Wrong - ConnectionFacade has not created.", connFacade);
        Assert.assertEquals("Wrong - diffrence between channel.isOpen() and ConnectionFacade.isAlive()", channel.isOpen(), connFacade.isAlive());
    }
}
