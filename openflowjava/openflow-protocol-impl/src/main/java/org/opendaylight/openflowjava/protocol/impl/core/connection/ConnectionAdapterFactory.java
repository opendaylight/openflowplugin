/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.openflowjava.protocol.impl.core.connection;

import io.netty.channel.Channel;
import java.net.InetSocketAddress;

/**
 * Factory for creating ConnectionFacade instances.
 *
 * @author mirehak
 * @author michal.polkorab
 */
public interface ConnectionAdapterFactory {

    /**
     * Creates a ConnectionFacade.
     *
     * @param ch {@link Channel} channel
     * @param address {@link InetSocketAddress}
     * @param useBarrier true to use a barrier, false otherwise
     * @param channelOutboundQueueSize configurable queue size
     * @return connection adapter tcp-implementation
     */
    ConnectionFacade createConnectionFacade(Channel ch, InetSocketAddress address, boolean useBarrier,
                                            int channelOutboundQueueSize);

}
