/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * factory for {@link QueueKeeper} implementations
 */
public abstract class QueueKeeperFactory {
    
    /**
     * @param sourceRegistrator 
     * @param capacity blocking queue capacity
     * @return fair reading implementation of {@link QueueKeeper}
     */
    @SuppressWarnings("resource")
    public static QueueKeeper<OfHeader> createFairQueueKeeper(
            MessageSourcePollRegistrator<QueueKeeper<OfHeader>> sourceRegistrator, int capacity) {
        QueueKeeperFairImpl queueKeeper = new QueueKeeperFairImpl();
        queueKeeper.setCapacity(capacity);
        queueKeeper.setHarvesterHandle(sourceRegistrator.getHarvesterHandle());
        queueKeeper.init();
        
        AutoCloseable registration = sourceRegistrator.registerMessageSource(queueKeeper);
        queueKeeper.setPollRegistration(registration);
        return queueKeeper;
    }

}
