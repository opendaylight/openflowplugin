/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * message counter (by type)
 */
public class MessageSpyCounterImpl implements MessageObservatory<OfHeader, DataObject> {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(MessageSpyCounterImpl.class);
    
    private Map<Class<? extends DataContainer>, AtomicLong[]> inputStats = new ConcurrentHashMap<>();
    
    @Override
    public void spyIn(OfHeader message) {
        Class<? extends DataContainer> msgType = message.getImplementedInterface();
        AtomicLong counter;
        synchronized(msgType) {
            AtomicLong[] counters = inputStats.get(msgType);
            if (counters == null) {
                counters = new AtomicLong[] {new AtomicLong(), new AtomicLong()};
                inputStats.put(msgType, counters);
            } 
            counter = counters[0];
        }
        counter.incrementAndGet();
    }

    @Override
    public void spyOut(List<DataObject> message) {
        // NOOP   
    }

    @Override
    public void run() {
        // log current counters and cleans it
        if (LOG.isDebugEnabled()) {
            for (String counterItem : dumpMessageCounts()) {
                LOG.debug(counterItem);
            }
        }
    }

    @Override
    public List<String> dumpMessageCounts() {
        List<String> dump = new ArrayList<>();
        for (Entry<Class<? extends DataContainer>, AtomicLong[]> statEntry : inputStats.entrySet()) {
            long amountPerInterval = statEntry.getValue()[0].getAndSet(0);
            long cumulativeAmount = statEntry.getValue()[1].addAndGet(amountPerInterval);
            dump.add(String.format("MSG[%s] -> +%d | %d", 
                    statEntry.getKey().getSimpleName(), amountPerInterval, cumulativeAmount));
        }
        return dump;
    }
}
