/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import java.net.InetSocketAddress;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandler;

/**
 *
 * @param <T>
 */
public class OutboundQueueManagerNoBarrier<T extends OutboundQueueHandler> extends
        AbstractOutboundQueueManager<T, StackedOutboundQueueNoBarrier> {

    OutboundQueueManagerNoBarrier(final ConnectionAdapterImpl parent, final InetSocketAddress address, final T handler) {
        super(parent, address, handler);
    }

    @Override
    protected StackedOutboundQueueNoBarrier initializeStackedOutboudnqueue() {
        return new StackedOutboundQueueNoBarrier(this);
    }

}
