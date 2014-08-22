/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.mp.MBodyMutablePortStatsRequest;
import org.opendaylight.of.lib.mp.MBodyPortStats;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.api.NotFoundException;
import org.opendaylight.util.net.BigPortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptySet;
import static org.opendaylight.of.lib.mp.MultipartType.PORT_STATS;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.ResourceUtils.getBundledResource;
import static org.opendaylight.util.StringUtils.format;

/**
 * Maintains the known state of all OpenFlow ports on all connected
 * OpenFlow capable devices, as well as providing the business logic for
 * fetching port statistics in a synchronous manner (from the caller's POV).
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Liem Nguyen
 */
class PortStateTracker {

    private static final ResourceBundle RES =
            getBundledResource(PortStateTracker.class);

    private static final String MSG_MISSING_DPID =
            RES.getString("msg_missing_dpid");

    private static final String E_NO_DATAPATH = RES.getString("e_no_datapath");
    private static final String E_NO_PORT = RES.getString("e_no_port_on_dp");
    private static final String E_NO_DATA = RES.getString("e_no_data");
    private static final String E_TIMEOUT = RES.getString("e_timeout");

    private static final long MAX_BLOCKED_WAIT_MS = 3000; // 3 seconds

    private Logger log = LoggerFactory.getLogger(OpenflowController.class);

    private final ListenerService ls;

    private final Map<DataPathId, DpPortState> stateCache =
            new ConcurrentHashMap<>();

    public PortStateTracker(ListenerService ls) {
        this.ls = ls;
    }

    @Override
    public String toString() {
        return "{PortStateTracker: mapSize=" + stateCache.size() + "}";
    }

    /**
     * Invoked by the listener manager when a new datapath connects to the
     * controller (or shortly thereafter). The list of ports reflects the
     * initial known state of the datapath's OpenFlow ports.
     *
     * @param dpid the source datapath
     * @param ports the ports
     */
    // TODO: Review - do we need to synchronize here somewhere? Anyone?
    void portInit(DataPathId dpid, List<Port> ports) {
        // just overwrite what is there with a new initial state
        stateCache.put(dpid, new DpPortState(dpid, ports));
    }

    /**
     * Invoked by the listener manager when a PortStatus message is received
     * from a datapath.
     *
     * @param msg the port status message
     * @param dpid the source datapath
     */
    void portStatus(OfmPortStatus msg, DataPathId dpid) {
        DpPortState dps = stateCache.get(dpid);
        if (dps != null)
            dps.processPortStatus(msg);
        else
            log.warn(MSG_MISSING_DPID, dpid);  // should never happen!
    }

    /**
     * Invoked by the listener manager when a datapath disconnects from
     * the controller.
     *
     * @param dpid the ID of the datapath that disconnected
     */
    // TODO: Review - synchronize?
    void dpRemoved(DataPathId dpid) {
        stateCache.remove(dpid);
    }

    /**
     * Returns a list that represents the known state of the ports for the
     * given datapath.
     *
     * @param dpid the requested datapath
     * @return the list of ports
     */
    // TODO: Review - synchronize?
    List<Port> getPorts(DataPathId dpid) {
        DpPortState dps = stateCache.get(dpid);
        return dps == null ? null : dps.getPorts();
    }

    /**
     * Validates that the give port actually exists on the given datapath.
     *
     * @param dpid dpid of the datapath
     * @param port port to validate
     * @throws NotFoundException if the port does not exist or the
     *          datapath does not exist
     */
    private void validatePort(DataPathId dpid, BigPortNumber port) {
        // NOTE: using the port state cache to validate the request...
        DpPortState dps = stateCache.get(dpid);
        if (dps == null)
            throw new NotFoundException(format(E_NO_DATAPATH, dpid));
        Port p = dps.getPort(port);
        if (p == null)
            throw new NotFoundException(format(E_NO_PORT, port.toLong(), dpid));
    }

    /**
     * Blocking call that fetches the stats for all ports of the given datapath.
     *
     * @param pv the protocol version
     * @param dpid the target datapath
     * @return the list of stats for all ports
     */
    List<MBodyPortStats> getPortStats(ProtocolVersion pv, DataPathId dpid) {
        OpenflowMessage msg =
                create(pv, MULTIPART_REQUEST, PORT_STATS).toImmutable();
        PortStatsFuture future = new PortStatsFuture(msg);
        pendingFutures.put(msg.getXid(), future);

        try {
            ls.send(msg, dpid);
        } catch (OpenflowException e) {
            pendingFutures.remove(msg.getXid());
            future.setFailure(e);
        }
        // wait for the stats to come in...
        await(future);
        return future.stats;
    }

    /**
     * Blocking call that fetches the stats for the specified port of
     * the given datapath.
     *
     * @param pv the protocol version
     * @param dpid the target datapath
     * @param port the target port
     * @return the list of stats for all ports
     */
    MBodyPortStats getPortStats(ProtocolVersion pv, DataPathId dpid,
                                BigPortNumber port) {
        validatePort(dpid, port);
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                create(pv, MULTIPART_REQUEST, PORT_STATS);
        ((MBodyMutablePortStatsRequest) req.getBody()).port(port);

        OpenflowMessage msg = req.toImmutable();
        PortStatsFuture future = new PortStatsFuture(msg);
        pendingFutures.put(msg.getXid(), future);

        try {
            ls.send(msg, dpid);
        } catch (OpenflowException e) {
            pendingFutures.remove(msg.getXid());
            future.setFailure(e);
        }
        // wait for the stats to come in...
        await(future);
        // assume that the requested port is the zeroth element
        if (future.stats.isEmpty())
            throw new NotFoundException(format(E_NO_PORT, port, dpid));
        return future.stats.get(0);
    }

    /**
     * Issues a PortMod message to the specified datapath with instructions
     * to either enable or disable the specified port.
     * A barrier request is used to ensure that the datapath accepted the
     * request.
     * A message future is returned allowing the caller to determine the
     * outcome of the request.
     *
     * @param pv the protocol version
     * @param dpid the target datapath
     * @param port the target port
     * @param enable true to enable; false to disable
     * @return a message future
     * @throws NullPointerException if any parameter is null
     * @throws NotFoundException if either the datapath does not exist, or
     *          the datapath does not have the specified port
     */
    MessageFuture enablePort(ProtocolVersion pv, DataPathId dpid,
                             BigPortNumber port, boolean enable) {
        validatePort(dpid, port);
        OpenflowMessage portMod = createPortMod(pv, dpid, port, enable);
        MessageFuture future = new DefaultMessageFuture(portMod);
        pendingFutures.put(portMod.getXid(), future);

        // send the port mod and follow up with barrier request
        try {
            ls.send(portMod, dpid);
            ls.send(barrierRequest(portMod), dpid);
        } catch (OpenflowException e) {
            pendingFutures.remove(portMod.getXid());
            future.setFailure(e);
        }
        return future;
    }

    // Generates a barrier request with XID matching that of given message
    private OpenflowMessage barrierRequest(OpenflowMessage msg) {
        return create(msg, BARRIER_REQUEST).toImmutable();
    }

    // Generates a port mod requesting enable/disable of a given port
    private OpenflowMessage createPortMod(ProtocolVersion pv, DataPathId dpid,
                                          BigPortNumber port, boolean enable) {
        OfmMutablePortMod portMod = (OfmMutablePortMod) create(pv, PORT_MOD);

        Set<PortConfig> flags = enable ? CONFIG_ENABLE : CONFIG_DISABLE;
        Port p = stateCache.get(dpid).getPort(port);
        portMod.port(port).hwAddress(p.getHwAddress()).config(flags)
                .configMask(CONFIG_MASK).advertise(ADV_NO_CHANGE);
        return portMod.toImmutable();
    }

    private static final Set<PortConfig> CONFIG_MASK =
            EnumSet.of(PortConfig.PORT_DOWN);
    private static final Set<PortConfig> CONFIG_ENABLE = emptySet();
    private static final Set<PortConfig> CONFIG_DISABLE =
            EnumSet.of(PortConfig.PORT_DOWN);
    private static final Set<PortFeature> ADV_NO_CHANGE = emptySet();

    // =====================================================================
    // === Unit test support

    Map<DataPathId, DpPortState> getCache() {
        return stateCache;
    }

    // =====================================================================
    // TODO: review - this pattern is repeated in PipelineManager, FlowTrk2, etc.


    // handle incoming port-stats replies
    void incomingPortStats(OfmMultipartReply reply) {
        PortStatsFuture f = (PortStatsFuture) pendingFutures.get(reply.getXid());
        // quick exit if it isn't ours
        if (f == null)
            return;
        // accumulate the port stats payload in the future
        f.stats.addAll(((MBodyPortStats.Array) reply.getBody()).getList());
        // If we do not expect any more replies, satisfy the future
        if (!reply.hasMore()) {
            pendingFutures.remove(reply.getXid());
            f.setSuccess(reply);
        }
    }

    // handle incoming barrier replies
    void incomingBarrier(OfmBarrierReply reply) {
        MessageFuture f = pendingFutures.get(reply.getXid());
        // quick exit if it isn't ours
        if (f == null)
            return;

        pendingFutures.remove(reply.getXid());
        f.setSuccess(reply);
    }

    // handle incoming errors
    void incomingError(OfmError error) {
        MessageFuture f = pendingFutures.get(error.getXid());
        // quick exit if it isn't ours
        if (f == null)
            return;

        // fail the future
        pendingFutures.remove(error.getXid());
        f.setFailure(error);
    }

    // Custom message future which collates results of multipart replies
    // for port statistics
    private static class PortStatsFuture extends DefaultMessageFuture {
        private final List<MBodyPortStats> stats = new ArrayList<>();
        public PortStatsFuture(OpenflowMessage request) {
            super(request);
        }
    }

    private final ConcurrentHashMap<Long, MessageFuture> pendingFutures =
            new ConcurrentHashMap<>();

    private void await(MessageFuture mf) {
        boolean completed = mf.awaitUninterruptibly(MAX_BLOCKED_WAIT_MS);
        if (!completed)
            throw new IllegalStateException(E_TIMEOUT);
        switch (mf.result()) {
            case SUCCESS:
                break;
            case OFM_ERROR:
                // TODO: Review which runtime exception to throw
                //  e.g. NotFoundException for OOB port number?
                throw new IllegalStateException(mf.reply().toString());
            case EXCEPTION:
                throw new IllegalStateException(E_NO_DATA, mf.cause());
        }
    }

    // =====================================================================

    /** Encapsulates the port status of a single datapath. */
    static class DpPortState {
        private final DataPathId dpid;
        private final Map<BigPortNumber, Port> portCache = new TreeMap<>();

        DpPortState(DataPathId dpid, List<Port> ports) {
            this.dpid = dpid;
            for (Port p: ports)
                portCache.put(p.getPortNumber(), p);
        }

        /**
         * Examines the given port status message and updates the port map
         * appropriately.
         *
         * @param msg the port status message
         */
        void processPortStatus(OfmPortStatus msg) {
            Port p = msg.getPort();
            BigPortNumber bpn = p.getPortNumber();
            switch (msg.getReason()) {
                case ADD:
                case MODIFY:
                    portCache.put(bpn, p);
                    break;
                case DELETE:
                    portCache.remove(bpn);
                    break;
            }
        }

        /**
         * Returns the datapath ID associated with this port state object.
         *
         * @return the datapath ID
         */
        DataPathId getDpid() {
            return dpid;
        }

        /**
         * Returns an ordered list of ports for this datapath, representing
         * the current state.
         *
         * @return the list of ports
         */
        List<Port> getPorts() {
            List<Port> list = new ArrayList<>(portCache.size());
            for (Map.Entry<BigPortNumber,Port> entry: portCache.entrySet())
                list.add(entry.getValue());
            return list;
        }

        /**
         * Returns the port registered under the given port number.
         *
         * @param p the port number
         * @return the corresponding port
         */
        Port getPort(BigPortNumber p) {
            return portCache.get(p);
        }
    }
}