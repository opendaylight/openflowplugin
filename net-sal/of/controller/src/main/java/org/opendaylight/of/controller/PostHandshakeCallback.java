/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller;

import org.opendaylight.of.lib.dt.DataPathId;

/**
 * Post hand-shake completion callback.
 *
 * @author Thomas Vachuska
 */
public interface PostHandshakeCallback {

    /**
     * Signals completion of the post-handshake task.
     *
     * @param dpid           datapath id
     * @param deviceTypeName device type name
     */
    void handshakeComplete(DataPathId dpid, String deviceTypeName);

}
