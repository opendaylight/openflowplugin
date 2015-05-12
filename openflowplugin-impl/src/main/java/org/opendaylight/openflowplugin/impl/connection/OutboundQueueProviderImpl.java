/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import javax.annotation.Nonnull;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInputBuilder;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 12.5.2015.
 */
public class OutboundQueueProviderImpl implements OutboundQueueProvider {

    private OutboundQueue outboundQueue;
    private final short ofVersion;

    public OutboundQueueProviderImpl(final short ofVersion) {
        this.ofVersion = ofVersion;
    }

    @Nonnull
    @Override
    public BarrierInput createBarrierRequest(@Nonnull final Long xid) {
        final BarrierInputBuilder biBuilder = new BarrierInputBuilder();
        biBuilder.setVersion(ofVersion);
        biBuilder.setXid(xid);
        return biBuilder.build();

    }

    @Override
    public void onConnectionQueueChanged(final OutboundQueue outboundQueue) {
        this.outboundQueue = outboundQueue;
    }

    @Override
    public OutboundQueue getOutboundQueue() {
        return this.outboundQueue;
    }
}
