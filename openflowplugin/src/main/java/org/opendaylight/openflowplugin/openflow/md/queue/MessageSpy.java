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
 * ticket spy - aimed on collecting intel about tickets 
 * @param <MSG_IN> type of incoming message
 * @param <MSG_OUT> type of outcoming message
 */
public interface MessageSpy<MSG_IN, MSG_OUT> extends Runnable {

    /**
     * @param message content of ticket
     */
    void spyIn(MSG_IN message);

    /**
     * @param message content of ticket
     */
    void spyOut(List<MSG_OUT> message);

}
