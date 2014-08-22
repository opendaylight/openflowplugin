/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology;

import org.opendaylight.net.model.ModelEvent;
import org.opendaylight.net.model.Topology;

import java.util.List;

/**
 * Represents an event in the network topology information model.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface TopologyEvent extends ModelEvent<Topology, TopologyEvent.Type> {

    /** Topology event types. */
    enum Type {
        /** Event represents topology change. */
        TOPOLOGY_CHANGED
    }

    /**
     * Returns the type of event.
     *
     * @return event type
     */
    Type type();

    /**
     * Returns the link subject of the event.
     *
     * @return event subject
     */
    @Override
    Topology subject();


    /**
     * Returns the device &amp; link events that triggered new topology
     * computation.
     *
     * @return list of trigger events
     */
    List<ModelEvent> reasons();

}
