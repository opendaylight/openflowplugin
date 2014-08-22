/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.OpenflowEvent;
import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.util.event.AbstractTypedEvent;

/**
 * A base implementation for OpenFlow events.
 *
 * @author Simon Hunt
 */
abstract class OpenflowEvt extends AbstractTypedEvent<OpenflowEventType>
        implements OpenflowEvent {

    OpenflowEvt(OpenflowEventType type) {
        super(type);
    }
}
