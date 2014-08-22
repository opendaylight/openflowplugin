/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */



package org.opendaylight.of.controller;

import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.util.net.IpAddress;

/**
 * An API for post-processing of newly connected datapaths.
 *
 * @author Simon Hunt
 */
public interface PostHandshakeSink {

    /**
     * Invokes the datapath post-handshake functionality.
     * <p/>
     * Note, this method should return quickly, since it is invoked from
     * an I/O loop thread; i.e. a processing task should be spawned on some
     * other thread.
     *
     * @param ip       the datapath remote IP address
     * @param dpid     the datapath ID
     * @param desc     the device description
     * @param callback completion callback
     * @return a reference to the task
     */
    PostHandshakeTask doPostHandshake(IpAddress ip, DataPathId dpid, MBodyDesc desc,
                                      PostHandshakeCallback callback);

}
