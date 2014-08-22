/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.flow;

import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.mp.MBodyFlowStats;
import org.opendaylight.of.lib.msg.FlowModCommand;
import org.opendaylight.of.lib.msg.MessageFuture;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.util.api.NotFoundException;

import java.util.List;

/**
 * Provides basic flow management for the controller.
 * <p>
 * Facilitates:
 * <ul>
 *     <li>
 *         the getting of flow statistics from a datapath,
 *         for one or all flow tables
 *     </li>
 *     <li>
 *         the sending of flows to a datapath
 *     </li>
 * </ul>
 * <p>
 * When sending messages, {@link MessageFuture}s are used to allow the caller
 * to determine the outcome of the request either synchronously or
 * asynchronously, at their preference.
 *
 * @author Radhika Hegde
 * @author Simon Hunt
 */
public interface FlowTracker {

    /** 
     * Returns flows installed in the specified table of the given datapath.
     * If the table ID is not given (i.e. {@code tableId} is {@code null}) then
     * all tables are assumed.
     * <p>
     * If the specified table ID is {@link TableId#ALL}, all flows from all
     * tables will be returned.
     * Note that table ID is ignored for OpenFlow 1.0 devices.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until all
     * the flow statistics replies have returned from the datapath.
     *
     * @param dpid the datapath from which the flows are requested
     * @param tableId the ID of the flow table
     * @return the list of flow statistics
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist
     */
    List<MBodyFlowStats> getFlowStats(DataPathId dpid, TableId tableId);

    /**
     * Sends the given <em>FlowMod</em> message to the specified datapath.
     * No confirmation is sought from the datapath; this method operates in
     * "fire and forget" mode. If acknowledgement is required, use 
     * {@link #sendConfirmedFlowMod} instead.
     * <p>
     * Adds the specified flow to the given datapath if the message's
     * command is {@link FlowModCommand#ADD ADD}.
     * Deletes the specified flow from the given datapath if the message's
     * command is {@link FlowModCommand#DELETE DELETE}.
     *
     * @param flowMod the <em>FlowMod</em> message to send
     * @param dpid the target datapath
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if flowMod is mutable
     * @throws NotFoundException if the specified datapath does not exist
     * @throws OpenflowException if there was an issue sending the message
     *
     * @see FlowModCommand
     */
    void sendFlowMod(OfmFlowMod flowMod, DataPathId dpid)
            throws OpenflowException;

    /**
     * Sends the given <em>FlowMod</em> message to the specified datapath.
     * A message future is returned, to allow the caller to discover the
     * outcome of the request once it has been satisfied.
     * <p>
     * Adds the specified flow to the given datapath if the message's
     * command is {@link FlowModCommand#ADD ADD}.
     * Deletes the specified flow from the given datapath if the message's
     * command is {@link FlowModCommand#DELETE DELETE}.
     *
     * @param flowMod the <em>FlowMod</em> message to send
     * @param dpid the target datapath
     * @return a message future
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if flowMod is mutable
     * @throws NotFoundException if the specified datapath does not exist
     * @throws OpenflowException if there was an issue sending the message
     *
     * @see FlowModCommand
     * @see MessageFuture
     */
    MessageFuture sendConfirmedFlowMod(OfmFlowMod flowMod, DataPathId dpid)
            throws OpenflowException;
}
