/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt;

// FIXME: We should move this to the .impl package to remove it from the
// public view as it is no longer the public facade.

import org.opendaylight.of.controller.MessageEvent;

/**
 * A packet sequencer sink allows submitting packet-in messages into the
 * <em>Packet-In</em> message event processing engine.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 * @author Frank Wood
 */
public interface PacketSequencerSink {
    
    /**
     * Processes the specified packet-in event.
     * Execution occurs in the current thread.
     * 
     * @param ev event to be processed
     */
    void processPacket(MessageEvent ev);

}
