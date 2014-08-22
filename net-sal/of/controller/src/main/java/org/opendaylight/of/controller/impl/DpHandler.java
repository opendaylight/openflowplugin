/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.DataPathEvent;
import org.opendaylight.of.controller.DataPathListener;
import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.net.IpAddress;

/**
* An event handler bound to the DataPath Listener/Event types.
*
* @author Simon Hunt
*/
class DpHandler extends EventHandler<DataPathListener, DataPathEvent> {
    DpHandler(DataPathListener listener, int qSize) {
        super(listener, qSize);
    }
    DpHandler(DataPathListener listener) {
        super(listener);
    }

    @Override
    protected DataPathEvent getResumedEvent() {
        return RESUMED_EVENT;
    }

    /** Singleton Resumed event. */
    private static final ResumedEvent RESUMED_EVENT = new ResumedEvent();

    /** Private implementation of MessageEvent that
     * has a type of DROPPED_EVENTS_CHECKPOINT.
     */
    private static class ResumedEvent
            extends OpenflowEvt implements DataPathEvent {

        ResumedEvent() {
            super(OpenflowEventType.DROPPED_EVENTS_CHECKPOINT);
        }

        @Override public DataPathId dpid() { return null; }
        @Override public ProtocolVersion negotiated() { return null; }
        @Override public IpAddress ip() { return null; }
    }
}
