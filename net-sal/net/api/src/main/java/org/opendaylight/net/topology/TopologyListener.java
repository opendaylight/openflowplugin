/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology;

/**
 * Entity capable of receiving asynchronous notifications about changes in
 * network topology.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface TopologyListener {

    /**
     * Receives a notification about a change in network topology model.
     *
     * @param event the topology event
     */
    void event(TopologyEvent event);

}
