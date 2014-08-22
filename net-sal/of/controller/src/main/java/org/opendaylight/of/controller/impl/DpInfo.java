/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;


import org.opendaylight.of.controller.ConnectionDetails;
import org.opendaylight.of.controller.DataPathDetails;
import org.opendaylight.of.controller.PostHandshakeTask;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.msg.Capability;
import org.opendaylight.of.lib.msg.DataPathMessageFuture;
import org.opendaylight.of.lib.msg.OfmFeaturesReply;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.cache.NotedAgeOutHashMap;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.PortNumber;

import java.util.*;

import static org.opendaylight.util.ResourceUtils.getBundledResource;


/**
 * Embodies the information about a datapath; that is, an aggregation
 * of all {@link OpenflowConnection}s from a specific datapath.
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Frank Wood
 */
class DpInfo implements Comparable<DpInfo> {

    private static final int MAIN_ID = 0;
    private static final int FUTURE_AGE_OUT_MS = 6000;

    private static final ResourceBundle RES =
            getBundledResource(DpInfo.class, "dpInfo");
    
    static final String E_NOT_MAIN_CONN = RES.getString("e_not_main_conn");
    static final String E_DUP_DPID = RES.getString("e_dup_dpid");

    private static final String E_DUP_AUX_CONN =
            RES.getString("e_dup_aux_conn");

    private static final String EMPTY = "";


    final DataPathId dpid;
    final Map<Integer, OpenflowConnection> conns;
    final OpenflowConnection mainConn;
    final ProtocolVersion negotiated;
    final OfmFeaturesReply features;
    final long readyAt;
    final NotedAgeOutHashMap<Long, DataPathMessageFuture> pendingFutures;
    final PortStateTracker portTracker;
    final OpenflowController controller;   // issuing controller instance

    // filled in at end of extended handshake...
    MBodyDesc deviceDescription;
    boolean noTableFeatures;

    // the post-handshake task associated with this datapath
    private PostHandshakeTask phTask;

    private boolean valid = true;

    // filled in once the device has been "typed" by the discovery engine
    private String deviceTypeName;

    /**
     * Constructs a new datapath info object from an initial connection
     * which should be a main connection (where {@code auxId == 0}). 
     * We are also given a reference to the port state tracker so that we 
     * can delegate when folks ask us for our ports.
     *
     * @param conn the initial connection
     * @param portTracker the port state tracker
     * @param controller issuing controller instance
     */
    DpInfo(OpenflowConnection conn, PortStateTracker portTracker,
           OpenflowController controller) {
        if (!conn.isMain())
            throw new IllegalStateException(E_NOT_MAIN_CONN + conn);

        dpid = conn.dpid;
        conns = new TreeMap<>();
        conns.put(MAIN_ID, conn);
        this.mainConn = conn;
        
        negotiated = conn.getNegotiated();
        features = conn.getFeaturesReply();
        readyAt = conn.readyAt;
        pendingFutures = new NotedAgeOutHashMap<>(FUTURE_AGE_OUT_MS, true);
        this.portTracker = portTracker;
        
        this.controller = controller;
    }

    @Override
    public String toString() {
        return "{DpInfo: dpid=" + dpid + ",#conns=" + conns.size() + "}";
    }

    /**
     * Returns a multi-line string representing this object instance,
     * useful for debugging.
     *
     * @return a multi-line string representation
     */
    public String toDebugString() {
        return toDebugString(0);
    }

    /**
     * Returns a multi-line string representing this object instance,
     * useful for debugging, using the specified number of spaces for
     * indent.
     *
     * @param indent number of spaces to indent
     * @return a multi-line string representation
     */
    public String toDebugString(int indent) {
        return toString();
    }

    /** Returns true if this datapath info instance is still valid;
     * that is, if the datapath this represents is still connected to the
     * controller.
     *
     * @return true if valid
     */
    boolean isValid() {
        return valid;
    }

    /** Invalidates this datapath info instance. To be called when the
     * datapath this represents disconnects from the controller.
     */
    void invalidate() {
        valid = false;
        if (phTask != null)
            phTask.invalidate();
    }

    /** 
     * Adds the specified connection to this "OpenFlow channel". This method is 
     * only called from {@link OpenflowController#newAuxConnectionReady}.
     *
     * @param conn the connection to add
     */
    void addConnection(OpenflowConnection conn) {
        // TODO: Validate connection state against main connection... ??
        //    e.g. that the negotiated protocol is the same
        //         reported nbuffers, ntables, etc????
        int auxId = conn.auxId;
        OpenflowConnection c = conns.get(auxId);
        if (c == null)
            conns.put(auxId, conn);
        else
            throw new IllegalStateException(E_DUP_AUX_CONN + conn);
    }

    /** Removes the specified connection from this "OpenFlow channel".
     *
     * @param conn the connection to remove
     */
    void removeConnection(OpenflowConnection conn) {
        int auxId = conn.auxId;
//        if (auxId == MAIN_ID) {
//            // TODO: should clean up all aux connections too!
//        }
        conns.remove(auxId);
    }

    /** Returns the connection matching the specified auxiliary ID, or null
     * if no such connection is registered.
     *
     * @param auxId the auxiliary channel ID
     * @return the corresponding connection
     */
    OpenflowConnection getConnection(int auxId) {
        return conns.get(auxId);
    }


    /**
     * Associates the device type with this device info.
     *
     * @param deviceTypeName the device type name
     */
    void setDeviceType(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }

    /**
     * Sets the post-handshake task reference.
     *
     * @param phTask the post-handshake task
     */
    void setPhTask(PostHandshakeTask phTask) {
        this.phTask = phTask;
    }

    /*
     * Implementation note: DpInfo instances are considered equivalent
     * if they have the same DataPathId.
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DpInfo)) return false;

        DpInfo dpInfo = (DpInfo) o;
        return dpid.equals(dpInfo.dpid);
    }

    @Override
    public int hashCode() {
        return dpid.hashCode();
    }

    @Override
    public int compareTo(DpInfo o) {
        return this.dpid.compareTo(o.dpid);
    }


    // =====================================================================
    // SNAPSHOTS

    /** 
     * Creates and returns a snapshot of this datapath info.
     *
     * @return a snapshot of this datapath info
     */
    DataPathInfo snapshot() {
        return new DpInfoSnapshot(this);
    }

    /** 
     * Creates and returns a detailed snapshot of this datapath info.
     *
     * @return a detailed snapshot of this datapath info
     */
    DataPathDetails detailedSnapshot() {
        return new DpDetailsSnapshot(this);
    }

    /** Private implementation of DataPathInfo. */
    class DpInfoSnapshot implements DataPathInfo,
            Comparable<DpInfoSnapshot> {

        private final DataPathId sdpid;
        private final IpAddress remoteAddress;
        private final PortNumber remotePort;
        private final ProtocolVersion pv;
        private final OfmFeaturesReply sfeatures;
        private final MBodyDesc sdesc;
        private final long sreadyAt;
        private final long lastMessageAt;

        DpInfoSnapshot(DpInfo info) {
            sdpid = info.dpid;
            remoteAddress = info.mainConn.remoteAddress;
            remotePort = info.mainConn.remotePort;
            pv = info.negotiated;
            sfeatures = info.features;
            sdesc = info.deviceDescription;
            sreadyAt = info.readyAt;
            // iterate across all connections and pick the most recent TS
            // TODO: Simplify to reflect just the main connection for speed
            long lastMax = 0;
            for (OpenflowConnection oc: info.conns.values())
                if (oc.lastMessageAt > lastMax)
                    lastMax = oc.lastMessageAt;
            lastMessageAt = lastMax;
        }

        /** 
         * Package-private access to the embedded features-reply.
         *
         * @return the features reply
         */
        OfmFeaturesReply features() {
            return sfeatures;
        }

        // TODO: toString()

        @Override
        public DataPathId dpid() {
            return sdpid;
        }

        @Override
        public ProtocolVersion negotiated() {
            return pv;
        }

        @Override
        public long readyAt() {
            return sreadyAt;
        }

        @Override
        public long lastMessageAt() {
            return lastMessageAt;
        }

        @Override
        public List<Port> ports() {
            List<Port> p = portTracker.getPorts(sdpid);
            return p != null ? p : new ArrayList<Port>(0);
        }

        @Override
        public long numBuffers() {
            return sfeatures.getNumBuffers();
        }

        @Override
        public int numTables() {
            return sfeatures.getNumTables();
        }

        @Override
        public Set<Capability> capabilities() {
            return sfeatures.getCapabilities();
        }

        @Override
        public IpAddress remoteAddress() {
            return remoteAddress;
        }

        @Override
        public PortNumber remotePort() {
            return remotePort;
        }

        @Override
        public String datapathDescription() {
        	return sdesc == null ? EMPTY : sdesc.getDpDesc();
        }

        @Override
        public String manufacturerDescription() {
        	return sdesc == null ? EMPTY : sdesc.getMfrDesc();
        }

        @Override
        public String hardwareDescription() {
        	return sdesc == null ? EMPTY : sdesc.getHwDesc();
        }

        @Override
        public String softwareDescription() {
        	return sdesc == null ? EMPTY : sdesc.getSwDesc();
        }

        @Override
        public String serialNumber() {
        	return sdesc == null ? EMPTY : sdesc.getSerialNum();
        }

        @Override
        public String deviceTypeName() {
            return deviceTypeName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DpInfoSnapshot)) return false;

            DpInfoSnapshot that = (DpInfoSnapshot) o;
            return sdpid.equals(that.sdpid);
        }

        @Override
        public int hashCode() {
            return sdpid.hashCode();
        }

        @Override
        public int compareTo(DpInfoSnapshot o) {
            return this.sdpid.compareTo(o.sdpid);
        }
    }

    /** Private implementation of DataPathDetails. */
    private class DpDetailsSnapshot extends DpInfoSnapshot
            implements DataPathDetails {

        private final Map<Integer, ConnectionDetails> sconns;

        private DpDetailsSnapshot(DpInfo info) {
            super(info);
            sconns = new TreeMap<>();
            for (Map.Entry<Integer, OpenflowConnection> entry:
                    info.conns.entrySet()) {
                sconns.put(entry.getKey(), entry.getValue().snapshot());
            }
        }

        // TODO: toString()

        @Override
        public Map<Integer, ConnectionDetails> connections() {
            return sconns;
        }

        @Override
        public ConnectionDetails mainConnection() {
            // pull the main connection out of the map.
            return sconns.get(MAIN_ID);
        }
    }
}