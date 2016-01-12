/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;


import org.opendaylight.openflowplugin.api.openflow.md.queue.MessageSourcePollRegistrator;

/**
 * closable registration for {@link org.opendaylight.openflowplugin.api.openflow.md.queue.MessageSourcePollRegistrator}
 * @param <IN> queue input message type
 */
public class MessageSourcePollRegistration<IN> implements AutoCloseable {
    
    private MessageSourcePollRegistrator<IN> messageSourceRegistry;
    private IN messageSource;
    
    /**
     * @param messageSourceRegistry message source registry
     * @param messageSource  message source
     */
    public MessageSourcePollRegistration(MessageSourcePollRegistrator<IN> messageSourceRegistry,
            IN messageSource) {
        this.messageSourceRegistry = messageSourceRegistry;
        this.messageSource = messageSource;
    }

    @Override
    public void close() throws Exception {
        messageSourceRegistry.unregisterMessageSource(messageSource);
    }
}
