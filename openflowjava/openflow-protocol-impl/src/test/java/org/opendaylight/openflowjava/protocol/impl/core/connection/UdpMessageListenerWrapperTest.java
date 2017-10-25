/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.impl.core.connection.UdpMessageListenerWrapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 *
 * @author madamjak
 *
 */
public class UdpMessageListenerWrapperTest {

    @Mock GenericFutureListener<Future<Void>> listener;
    @Mock OfHeader msg;

    @Before
    public void startUp(){
        MockitoAnnotations.initMocks(this);
    }
    /**
     * Getters test
     */
    @Test
    public void test(){
        int port = 9876;
        String host ="localhost";
        InetSocketAddress inetSockAddr = InetSocketAddress.createUnresolved(host, port);
        UdpMessageListenerWrapper wrapper = new UdpMessageListenerWrapper(msg,listener,inetSockAddr);

        Assert.assertEquals("Wrong getAddress", inetSockAddr, wrapper.getAddress());
        Assert.assertEquals("Wrong getListener", listener, wrapper.getListener());
        Assert.assertEquals("Wrong getMsg", msg, wrapper.getMsg());
    }

}
