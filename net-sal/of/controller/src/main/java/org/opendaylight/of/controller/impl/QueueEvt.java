/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.of.controller.QueueEvent;

/**
 * A queue event.
 *
 * @author Simon Hunt
 */
class QueueEvt extends OpenflowEvt implements QueueEvent {

    QueueEvt(OpenflowEventType type) {
        super(type);
    }

}
