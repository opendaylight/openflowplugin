/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.flow;

import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.mp.MBodyGroupDescStats;
import org.opendaylight.of.lib.mp.MBodyGroupFeatures;
import org.opendaylight.of.lib.mp.MBodyGroupStats;
import org.opendaylight.of.lib.msg.GroupModCommand;
import org.opendaylight.of.lib.msg.MessageFuture;
import org.opendaylight.of.lib.msg.OfmGroupMod;
import org.opendaylight.util.api.NotFoundException;

import java.util.List;

/**
 * Provides basic group management for the controller.
 * <p>
 * Facilitates:
 * <ul>
 *     <li>
 *         the getting of group descriptions from a datapath,
 *         for one or all groups
 *     </li>
 *      <li>
 *         the getting of group statistics from a datapath,
 *         for one or all groups
 *     </li>
 *     <li>
 *         the sending of group configuration to a datapath
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
public interface GroupTracker {

    /**
     * Returns the group descriptions from the specified datapath.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until all
     * the group description replies have returned from the datapath.
     *
     * @param dpid the target datapath
     * @return the list of group descriptions
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist
     * @throws VersionMismatchException if datapath is not
     *          OpenFlow 1.3 compliant
     */
    List<MBodyGroupDescStats> getGroupDescription(DataPathId dpid);

    /**
     * Returns the group description for the given group ID, from the
     * specified datapath.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until the
     * group description reply has returned from the datapath.
     *
     * @param dpid the target datapath
     * @param groupId the ID of the group
     * @return the group description
     * @throws NullPointerException if either parameter is null
     * @throws NotFoundException if the specified datapath or group
     *          does not exist
     * @throws VersionMismatchException if datapath is not
     *          OpenFlow 1.3 compliant
     */
    MBodyGroupDescStats getGroupDescription(DataPathId dpid, GroupId groupId);

    /**
     * Returns the group statistics from the specified datapath.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until all
     * the group statistics replies have returned from the datapath.
     *
     * @param dpid the target datapath
     * @return the list of group statistics
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist
     * @throws VersionMismatchException if datapath is not
     *          OpenFlow 1.3 compliant
     */
    List<MBodyGroupStats> getGroupStats(DataPathId dpid);

    /**
     * Returns the group statistics for the given group ID, from the specified
     * datapath.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until
     * the group statistics reply has returned from the datapath.
     *
     * @param dpid the target datapath
     * @param groupId the target group ID
     * @return the group statistics
     * @throws NullPointerException if either parameter is null
     * @throws NotFoundException if the specified datapath or group
     *          does not exist
     * @throws VersionMismatchException if datapath is not
     *          OpenFlow 1.3 compliant
     */
    MBodyGroupStats getGroupStats(DataPathId dpid, GroupId groupId);

    /**
     * Returns the supported group features for the specified datapath.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until
     * the group features reply has returned from the datapath.
     *
     * @param dpid the target datapath
     * @return the group features
     * @throws NullPointerException if dpid id null
     * @throws NotFoundException if the specified datapath does not exist
     * @throws VersionMismatchException if datapath is not
     *          OpenFlow 1.3 compliant
     */
    MBodyGroupFeatures getGroupFeatures(DataPathId dpid);

    /**
     * Sends the given <em>GroupMod</em> message to the specified datapath.
     * A message future is returned, to allow the caller to discover the
     * outcome of the request once it has been satisfied.
     * <p>
     * Adds the group entry to the specified datapath if the message's
     * command is {@link GroupModCommand#ADD ADD}.
     * Modifies the group entry on the specified datapath if the message's
     * command is {@link GroupModCommand#MODIFY MODIFY}.
     * Deletes the group entry from the specified datapath if the message's
     * command is {@link GroupModCommand#DELETE DELETE}.
     *
     * @param groupMod the <em>GroupMod</em> message to send
     * @param dpid the target datapath
     * @return a message future
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if groupMod is mutable
     * @throws NotFoundException if the specified datapath does not exist
     * @throws OpenflowException if there was an issue sending the message
     *
     * @see GroupModCommand
     */
    MessageFuture sendGroupMod(OfmGroupMod groupMod, DataPathId dpid)
            throws OpenflowException;

}
