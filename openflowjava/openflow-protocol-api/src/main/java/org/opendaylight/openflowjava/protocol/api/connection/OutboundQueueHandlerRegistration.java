/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.connection;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

/**
 * An {@link ObjectRegistration} of a {@link OutboundQueueHandler}. Registration can be cancelled
 * by invoking {@link #close()}.
 *
 * @param <T> Handler type
 */
@Beta
public interface OutboundQueueHandlerRegistration<T extends OutboundQueueHandler> extends ObjectRegistration<T> {
    @Override
    void close();
}
