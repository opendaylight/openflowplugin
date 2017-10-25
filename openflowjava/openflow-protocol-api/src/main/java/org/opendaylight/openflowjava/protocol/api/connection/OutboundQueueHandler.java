/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.connection;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;

/**
 * Handler of the outbound queue. The queue has a maximum depth assigned when the
 * handler is registered.
 */
@Beta
public interface OutboundQueueHandler {
    /**
     * Create a new {@link BarrierInput barrier} message. This callback is invoked
     * when the queue is being flushed to the switch. The barrier ensures that any
     * outstanding requests are detected as either completed or failed.
     *
     * @param xid XID for the barrier message
     * @return New barrier message.
     */
    @Nonnull BarrierInput createBarrierRequest(@Nonnull Long xid);

    /**
     * Invoked whenever the underlying queue is refreshed. Implementations should
     * ensure they are talking to the latest queue
     * @param queue New queue instance, null indicates a shutdown, e.g. the queue
     *              is no longer available.
     */
    void onConnectionQueueChanged(OutboundQueue queue);
}
