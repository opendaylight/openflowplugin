/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.queue;

import java.util.Collection;

/**
 * MessageSourcePollRegistrator.
 * @param <I> message wrapping type (IN)
 *
 */
public interface MessageSourcePollRegistrator<I> {

    /**
     * Message source to read from during processing.
     * @param messageSource to read from during processing
     * @return closeable registration
     */
    AutoCloseable registerMessageSource(I messageSource);

    /**
     * Unregister message source.
     * @param messageSource to be unregistered
     * @return true if successfully unregistered
     */
    boolean unregisterMessageSource(I messageSource);

    /**
     * Getter.
     * @return collection of registered message sources
     */
    Collection<I> getMessageSources();

    /**
     * Getter.
     * @return the harvest handle
     */
    HarvesterHandle getHarvesterHandle();
}
