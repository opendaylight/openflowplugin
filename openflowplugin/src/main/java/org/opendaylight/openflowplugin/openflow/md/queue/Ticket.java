/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.yangtools.yang.binding.DataObject;

import com.google.common.util.concurrent.SettableFuture;

/**
 * @author mirehak
 *
 * @param <T> resulting type of process ticket
 */
public interface Ticket<T> {

    /**
     * @return processed result
     */
    SettableFuture<T> getResult();

    /**
     * @return registered type of processed message
     */
    Class<? extends DataObject> getRegisteredMessageType();

    /**
     * @return connection wrapper
     */
    ConnectionConductor getConductor();

    /**
     * @return processed message
     */
    DataObject getMessage();
}
