/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.openflowplugin.openflow.md.queue.PopListener;

/**
 * @author mirehak
 * @param <T> resulting type
 *
 */
public class PopListenerCountingImpl<T> implements PopListener<T> {

    private int count = 0;

    @Override
    public synchronized void onPop(T processedMessage) {
        count ++;
        notify();
    }
    
    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

}
