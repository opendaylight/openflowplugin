/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.MessageListener;
import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.MessageType;
import org.opendaylight.of.lib.msg.OpenflowMessage;

import java.util.Set;

/**
 * An event handler bound to the Message Listener/Event types.
 *
* @author Simon Hunt
*/
class MsgHandler extends EventHandler<MessageListener, MessageEvent> {
    MsgHandler(MessageListener listener, Set<MessageType> types, int qSize) {
        super(listener, types, qSize);
    }
    MsgHandler(MessageListener listener) {
        super(listener);
    }

    @Override
    protected MessageEvent getResumedEvent() {
        return RESUMED_EVENT;
    }

    /** Singleton Resumed event. */
    private static final ResumedEvent RESUMED_EVENT = new ResumedEvent();

    /** Private implementation of MessageEvent that
     * has a type of DROPPED_EVENTS_CHECKPOINT.
     */
    private static class ResumedEvent
            extends OpenflowEvt implements MessageEvent {

        ResumedEvent() {
            super(OpenflowEventType.DROPPED_EVENTS_CHECKPOINT);
        }

        @Override public OpenflowMessage msg() { return null; }
        @Override public DataPathId dpid() { return null; }
        @Override public int auxId() { return 0; }
        @Override public ProtocolVersion negotiated() { return null; }
        @Override public String remoteId() { return null; }
    }

}
