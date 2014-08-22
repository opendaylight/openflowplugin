/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

import org.opendaylight.of.lib.dt.DataPathInfo;

import java.util.Map;

/**
 * Extends {@link org.opendaylight.of.lib.dt.DataPathInfo} to provide additional information
 * about an OpenFlow datapath; specifically, details of the
 * currently available network connections.
 *
 * @author Simon Hunt
 */
public interface DataPathDetails extends DataPathInfo {

    /** An aggregation of all currently available connections to the
     * datapath. The key is the connection's auxiliary id.
     * Note that the map will include the "main" connection
     * (auxiliary id == 0).
     *
     * @return all currently available connections, mapped by auxiliary id
     */
    Map<Integer, ConnectionDetails> connections();

    /** The "main" connection; that is, the connection with auxiliary id == 0.
     *
     * @return details of the "main" connection
     */
    ConnectionDetails mainConnection();
}
