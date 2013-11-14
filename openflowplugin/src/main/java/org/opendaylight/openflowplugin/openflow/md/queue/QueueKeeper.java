/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.Collection;
import java.util.Map;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageListener;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author mirehak
 * @param <T> result type
 *
 */
public interface QueueKeeper<T> {

    /**
     * @param listener
     */
    void addPopListener(PopListener<T> listener);

    /**
     * @param listener
     * @return removed listener
     */
    boolean removePopListener(PopListener<T> listener);

    /**
     * @param listenerMapping
     */
    void setListenerMapping(Map<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping);

    /**
     * @param registeredMessageClazz registered message type
     * @param message
     * @param conductor
     */
    void push(Class<? extends DataObject> registeredMessageClazz, DataObject message,
            ConnectionConductor conductor);
}
