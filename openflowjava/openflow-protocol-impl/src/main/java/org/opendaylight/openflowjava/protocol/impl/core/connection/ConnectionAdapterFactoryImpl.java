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
 * @author mirehak
 * @author michal.polkorab
 */
public class ConnectionAdapterFactoryImpl implements ConnectionAdapterFactory {

    /**
     * @param ch
     * @return connection adapter tcp-implementation
     */
	@Override
    public ConnectionFacade createConnectionFacade(final Channel ch, final InetSocketAddress address,
            final boolean useBarrier) {
        return new ConnectionAdapterImpl(ch, address, useBarrier);
    }

}
