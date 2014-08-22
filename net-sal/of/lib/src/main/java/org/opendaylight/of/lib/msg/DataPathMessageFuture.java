/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.dt.DataPathId;

/**
 * A {@link DefaultMessageFuture} that is associated with a specific datapath.
 *
 * @author Frank Wood
 * @author Simon Hunt
 */
public class DataPathMessageFuture extends DefaultMessageFuture {

    private static final String E_NULL_DPID = "dpid cannot be null";

    private final DataPathId dpid;

    /**
     * Constructs a message future for the given request, destined for the
     * specified datapath.
     *
     * @param request the out-bound request associated with this futuer
     * @param dpid the target datapath
     */
    public DataPathMessageFuture(OpenflowMessage request, DataPathId dpid) {
        super(request);
        if (dpid == null)
            throw new NullPointerException(E_NULL_DPID);
        this.dpid = dpid;
    }
    
    /**
     * Returns the datapath ID associated with this future.
     * 
     * @return the datapath ID
     */
    public DataPathId dpid() {
        return dpid;
    }
}
