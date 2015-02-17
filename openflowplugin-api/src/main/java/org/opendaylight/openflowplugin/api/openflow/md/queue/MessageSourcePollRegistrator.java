/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.queue;

import java.util.Collection;

/**
 * @param <I> message wrapping type (IN)
 *
 */
public interface MessageSourcePollRegistrator<I> {

    /**
     * @param messageSource to read from during processing
     * @return closeable registration
     */
    AutoCloseable registerMessageSource(I messageSource);

    /**
     * @param messageSource to be unregistered
     * @return true if successfully unregistered
     */
    boolean unregisterMessageSource(I messageSource);

    /**
     * @return collection of registered message sources
     */
    Collection<I> getMessageSources();

    /**
     * @return the harvest handle
     */
    HarvesterHandle getHarvesterHandle();
}
