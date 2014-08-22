/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology;

import org.opendaylight.net.model.AbstractModelEvent;
import org.opendaylight.net.model.ModelEvent;
import org.opendaylight.net.model.Topology;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the topology event.
 *
 * @author Thomas Vachuska
 */
public class DefaultTopologyEvent extends AbstractModelEvent<Topology, TopologyEvent.Type>
        implements TopologyEvent {

    private final List<ModelEvent> reasons;

    /**
     * Constructs a topology event with the topology subject.
     *
     * @param subject the link associated with the event
     * @param reasons optional list of trigger events
     */
    public DefaultTopologyEvent(Topology subject, List<ModelEvent> reasons) {
        this(Type.TOPOLOGY_CHANGED, subject, reasons);
    }

    /**
     * Constructs a topology event with the given type and topology.
     *
     * @param type    the type of event
     * @param subject the link associated with the event
     * @param reasons optional list of trigger events
     */
    public DefaultTopologyEvent(Type type, Topology subject,
                                List<ModelEvent> reasons) {
        super(type, subject);
        this.reasons = reasons == null ? null : new ArrayList<>(reasons);
    }

    @Override
    public List<ModelEvent> reasons() {
        return reasons;
    }

}
