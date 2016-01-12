/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import org.opendaylight.openflowplugin.api.openflow.md.queue.MessageSourcePollRegistrator;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper;
import org.opendaylight.openflowplugin.api.openflow.md.queue.WaterMarkListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * factory for
 * {@link org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper}
 * implementations
 */
public abstract class QueueKeeperFactory {

    /**
     * @param sourceRegistrator source registrator
     * @param capacity blocking queue capacity
     * @param waterMarkListener water mark listener
     * @return fair reading implementation of
     *         {@link org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper}
     *         (not registered = not started yet)
     */
    public static QueueKeeper<OfHeader> createFairQueueKeeper(
            MessageSourcePollRegistrator<QueueKeeper<OfHeader>> sourceRegistrator,
            int capacity, WaterMarkListener waterMarkListener) {
        QueueKeeperFairImpl queueKeeper = new QueueKeeperFairImpl();
        queueKeeper.setCapacity(capacity);
        queueKeeper.setHarvesterHandle(sourceRegistrator.getHarvesterHandle());
        queueKeeper.setWaterMarkListener(waterMarkListener);
        queueKeeper.init();

        return queueKeeper;
    }

    /**
     * register queue by harvester, start processing it. Use
     * {@link QueueKeeperFairImpl#close()} to kill the queue and stop
     * processing.
     * 
     * @param sourceRegistrator source registrator
     * @param queueKeeper queue keeper
     */
    public static <V> void plugQueue(
            MessageSourcePollRegistrator<QueueKeeper<V>> sourceRegistrator,
            QueueKeeper<V> queueKeeper) {
        AutoCloseable registration = sourceRegistrator
                .registerMessageSource(queueKeeper);
        queueKeeper.setPollRegistration(registration);
        sourceRegistrator.getHarvesterHandle().ping();
    }
}
