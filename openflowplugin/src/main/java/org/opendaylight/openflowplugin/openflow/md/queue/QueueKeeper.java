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
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.TranslatorKey;

/**
 * @author mirehak
 * @param <IN> source type
 * @param <OUT> result type
 */
public interface QueueKeeper<IN, OUT> {
    
    /**
     * @param listener
     */
    void addPopListener(PopListener<OUT> listener);
    
    /**
     * @param listener
     * @return removed listener
     */
    boolean removePopListener(PopListener<OUT> listener);
    
    /**
     * @param translatorMapping
     */
    void setTranslatorMapping(Map<TranslatorKey, Collection<IMDMessageTranslator<IN, OUT>>> translatorMapping);

    /**
     * @param registeredMessageClazz registered message type
     * @param message
     * @param conductor
     */
    void push(Class<? extends IN> registeredMessageClazz, IN message, 
            ConnectionConductor conductor);
}
