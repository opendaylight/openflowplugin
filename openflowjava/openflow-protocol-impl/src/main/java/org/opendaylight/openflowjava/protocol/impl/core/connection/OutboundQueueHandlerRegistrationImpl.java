/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandler;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;

abstract class OutboundQueueHandlerRegistrationImpl<T extends OutboundQueueHandler> extends AbstractObjectRegistration<T> implements OutboundQueueHandlerRegistration<T> {
    protected OutboundQueueHandlerRegistrationImpl(final T instance) {
        super(instance);
    }
}
