/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author madamjak
 *
 */

public class VersionMessageUdpWrapperTest {

    @Mock ByteBuf byteBuff;
    @Before
    public void startUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test(){
        short version = 35;
        int port = 9876;
        String host ="localhost";
        InetSocketAddress inetSockAddr = InetSocketAddress.createUnresolved(host, port);
        VersionMessageUdpWrapper wrapper = new VersionMessageUdpWrapper(version,byteBuff,inetSockAddr);

        Assert.assertEquals("Wrong getAddress", inetSockAddr, wrapper.getAddress());
        Assert.assertEquals("Wrong getVersion", version, wrapper.getVersion());
        Assert.assertEquals("Wrong getVersion", byteBuff, wrapper.getMessageBuffer());
    }
}
