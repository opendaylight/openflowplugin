/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.List;

import org.opendaylight.openflowplugin.openflow.md.queue.PopListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 * @param <T> resulting type
 *
 */
public class PopListenerCountingImpl<T> implements PopListener<T> {

    private static final Logger LOG = LoggerFactory
            .getLogger(PopListenerCountingImpl.class);
    private int count = 0;

    @Override
    public synchronized void onPop(List<T> processedMessage) {
        LOG.debug("message popped: {}", processedMessage);
        count += processedMessage.size();
        notify();
    }
    
    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

}
