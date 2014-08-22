/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology;

import org.opendaylight.net.model.ModelEvent;
import org.opendaylight.net.supplier.SupplierService;

import java.util.List;

/**
 * Set of services available for topology suppliers to contribute information
 * about network topology.
 *
 * @author Thomas Vachuska
 */
public interface TopologySupplierService extends SupplierService {

    /**
     * Submits the specified topology data to be used as an active topology.
     *
     * @param topologyData processed topology data
     * @param reasons      optional list of reasons
     */
    void submit(TopologyData topologyData, List<ModelEvent> reasons);

}
