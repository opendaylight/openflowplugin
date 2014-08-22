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
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.mp.MBodyExperimenter;
import org.opendaylight.of.lib.mp.MBodyMeterConfig;
import org.opendaylight.of.lib.mp.MBodyMeterFeatures;
import org.opendaylight.of.lib.mp.MBodyMeterStats;
import org.opendaylight.of.lib.msg.MessageFuture;
import org.opendaylight.of.lib.msg.MeterModCommand;
import org.opendaylight.of.lib.msg.OfmMeterMod;
import org.opendaylight.util.api.NotFoundException;

import java.util.List;

/**
 * Provides basic meter management for the controller.
 * <p>
 * Facilitates:
 * <ul>
 *     <li>
 *         obtaining meter configuration details from a datapath,
 *         for one or all meters
 *     </li>
 *     <li>
 *         obtaining meter statistics from a datapath,
 *         for one or all meters
 *     </li>
 *     <li>
 *         obtaining meter features from a datapath
 *     </li>
 *     <li>
 *         sending meter configuration to a datapath
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
public interface MeterTracker {

    /**
     * Returns the meter configuration details from the specified datapath.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until all
     * the meter configuration replies have returned from the datapath.
     *
     * @param dpid the target datapath
     * @return the list of meter configurations
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist
     * @throws VersionMismatchException if datapath is not
     *          OpenFlow 1.3 compliant
     */
    List<MBodyMeterConfig> getMeterConfig(DataPathId dpid);

    /**
     * Returns the meter configuration for the given meter ID, from the
     * specified datapath.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until all
     * the meter configuration reply has returned from the datapath.
     *
     * @param dpid the target datapath
     * @param meterId the ID of the meter
     * @return the meter configuration
     * @throws NullPointerException if either parameter is null
     * @throws NotFoundException if the specified datapath or meter
     *          does not exist
     * @throws VersionMismatchException if datapath is not
     *          OpenFlow 1.3 compliant
     */
    MBodyMeterConfig getMeterConfig(DataPathId dpid, MeterId meterId);

    /**
     * Returns the meter statistics from the specified datapath.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until all
     * the meter statistics replies have returned from the datapath.
     *
     * @param dpid the target datapath
     * @return the list of meter statistics
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist
     * @throws VersionMismatchException if datapath is not
     *          OpenFlow 1.3 compliant
     */
    List<MBodyMeterStats> getMeterStats(DataPathId dpid);

    /**
     * Returns the meter statistics for the given meter ID, from the specified
     * datapath.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until
     * the meter statistics reply has returned from the datapath.
     *
     * @param dpid the target datapath
     * @param meterId the target meter ID
     * @return the meter statistics
     * @throws NullPointerException if either parameter is null
     * @throws NotFoundException if the specified datapath or meter
     *          does not exist
     * @throws VersionMismatchException if datapath is not
     *          OpenFlow 1.3 compliant
     */
    MBodyMeterStats getMeterStats(DataPathId dpid, MeterId meterId);

    /**
     * Returns the meter features for the specified datapath.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until the
     * meter features reply has returned from the datapath.
     *
     * @param dpid the target datapath
     * @return the meter features
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist
     * @throws VersionMismatchException if datapath is not
     *          OpenFlow 1.3 compliant
     */
    MBodyMeterFeatures getMeterFeatures(DataPathId dpid);

    // === Support for meters on 1.0 devices, via Experimenter extensions

    // TODO: add a method to send encapsulated experimenter message
    //          which is cognizant of custom meter format

    /**
     * Returns meter configuration or statistics (for OF 1.0 datapaths),
     * encoded in experimenter multipart replies.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until
     * the replies have returned from the datapath.
     *
     * @param dpid the target datapath
     * @return the list of configuations or statistics
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist
     */
    // TODO : return List<SomeEncapsulatingClass> getSomethingSpecific()
    List<MBodyExperimenter> getExperimenter(DataPathId dpid);

    /**
     * Sends the given <em>MeterMod</em> message to the specified datapath.
     * A message future is returned, to allow the caller to discover the
     * outcome of the request once it has been satisfied.
     * <p>
     * Adds the meter to the specified datapath if the message's
     * command is {@link MeterModCommand#ADD ADD}.
     * Modifies the meter on the specified datapath if the message's
     * command is {@link MeterModCommand#MODIFY MODIFY}.
     * Deletes the meter from the specified datapath if the message's
     * command is {@link MeterModCommand#DELETE DELETE}.
     *
     * @param meterMod the <em>MeterMod</em> message to send
     * @param dpid the target datapath
     * @return a message future
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if meterMod is mutable
     * @throws NotFoundException if the specified datapath does not exist
     * @throws OpenflowException if there was an issue sending the message
     *
     * @see MeterModCommand
     */
    MessageFuture sendMeterMod(OfmMeterMod meterMod, DataPathId dpid)
       throws OpenflowException;

}
