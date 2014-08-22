/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OpenflowMessage;

/**
 * A message event where we don't know the datapath ID or auxiliary connection
 * ID up front.
 * What we do instead is have a datapath ID future which the controller will
 * satisfy at the end of the handshake sequence, once the dpid and aux ID
 * are known.
 *
 * @author Simon Hunt
 */
class HandshakeMessageEvt extends MessageEvt {

    DpidFuture dpidFuture;
    private volatile DataPathId knownDpid;
    private volatile int knownAuxId;
    private volatile ProtocolVersion knownNegotiated;
    private OpenflowConnection conn;

    /** Constructs a record of a "handshake" message transmission.
     *
     * @param type the event type
     * @param msg the message
     * @param connection the connection information
     */
    HandshakeMessageEvt(OpenflowEventType type, OpenflowMessage msg,
                        OpenflowConnection connection) {
        super(type, msg, null, -1, null);
        this.conn = connection;
        dpidFuture = new DpidFuture(connection);
    }

    @Override
    public String remoteId() {
        synchronized (this) {
            return knownDpid != null ? knownDpid.toString() : remoteStr();
        } // sync
    }

    @Override
    public DataPathId dpid() {
        synchronized (this) {
            return knownDpid;
        } // sync
    }

    @Override
    public int auxId() {
        synchronized (this) {
            return knownAuxId;
        } // sync
    }

    @Override
    public ProtocolVersion negotiated() {
        synchronized (this) {
            return knownNegotiated;
        } // sync
    }

    private String remoteStr() {
        return String.valueOf(conn.remoteAddress) + "/" + conn.remotePort;
    }

    //=======================================================================

    /** Represents the promise of a DataPathId, auxiliary channel ID, and
     * negotiated protocol version, sometime in the near future.
     */
    class DpidFuture {
        final OpenflowConnection connection;

        /** Creates the future.
         *
         * @param connection the associated connection
         */
        private DpidFuture(OpenflowConnection connection) {
            this.connection = connection;
        }

        /** Satisfies the future.
         *
         * @param dpid the dpid, now that we know what it is
         * @param auxId the auxiliary channel ID, now that we know what it is
         * @param pv the negotiated version, now that we know what it is
         */
        void satisfy(DataPathId dpid, int auxId, ProtocolVersion pv) {
            synchronized (HandshakeMessageEvt.this) {
                if (dpid != null) {
                    HandshakeMessageEvt.this.knownDpid = dpid;
                    HandshakeMessageEvt.this.knownAuxId = auxId;
                    HandshakeMessageEvt.this.knownNegotiated = pv;
                    HandshakeMessageEvt.this.dpidFuture = null;
                    HandshakeMessageEvt.this.conn = null;
                }
            } // sync
        }
    }
}
