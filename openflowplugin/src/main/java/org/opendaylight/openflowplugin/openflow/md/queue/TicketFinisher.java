/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.List;

/**
 * @param <OUT> result type
 *
 */
public interface TicketFinisher<OUT> extends Runnable {

    /**
     * initiate shutdown of this worker
     */
    void finish();
    
    /**
     * notify popListeners
     * @param processedMessages processed message
     */
    void firePopNotification(List<OUT> processedMessages);
}
