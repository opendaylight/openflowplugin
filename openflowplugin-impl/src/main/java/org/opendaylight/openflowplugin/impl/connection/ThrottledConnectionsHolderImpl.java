/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.connection.ThrottledConnectionsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.5.2015.
 */
public class ThrottledConnectionsHolderImpl implements ThrottledConnectionsHolder, TimerTask {

    private final Set<ConnectionAdapter> throttledConnections = Collections.synchronizedSet(new LinkedHashSet<ConnectionAdapter>());
    private final HashedWheelTimer hashedWheelTimer;
    private Timeout timeout;
    private long delay = 100L;
    private static final Logger LOG = LoggerFactory.getLogger(ThrottledConnectionsHolderImpl.class);

    public ThrottledConnectionsHolderImpl(final HashedWheelTimer hashedWheelTimer) {
        this.hashedWheelTimer = hashedWheelTimer;
    }

    @Override
    public void storeThrottledConnection(final ConnectionAdapter connectionAdapter) {
        throttledConnections.add(connectionAdapter);
        LOG.info("Adding piece of throttle for {}", connectionAdapter.getRemoteAddress());
        synchronized (this) {
            if (null == timeout) {
                scheduleTimeout();
            }
        }
    }

    private void scheduleTimeout() {
        this.timeout = hashedWheelTimer.newTimeout(this, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run(final Timeout timeout) throws Exception {
        synchronized (this) {
            this.timeout = null;
            if (throttledConnections.isEmpty()) {
                return;
            }

            final Iterator<ConnectionAdapter> iterator = throttledConnections.iterator();
            if (iterator.hasNext()) {
                ConnectionAdapter connectionAdapter = iterator.next();
                iterator.remove();
                connectionAdapter.setAutoRead(true);
                LOG.info("Un - throttling primary connection for {}", connectionAdapter.getRemoteAddress());
            }

            scheduleTimeout();
        }
    }
}
