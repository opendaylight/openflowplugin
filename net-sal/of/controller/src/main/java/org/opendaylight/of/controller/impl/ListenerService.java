/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

// FIXME: We should move this to the .impl package
// ...to remove it from the public view as it is no longer the public facade.

import org.opendaylight.of.controller.ControllerService;
import org.opendaylight.of.controller.ControllerStats;
import org.opendaylight.of.controller.DataPathListener;
import org.opendaylight.of.controller.MessageListener;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MBodyPortStats;
import org.opendaylight.of.lib.mp.MBodyTableFeatures;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.api.NotFoundException;
import org.opendaylight.util.net.BigPortNumber;

import java.util.List;
import java.util.Set;

/**
 * Provides the public API to the OpenFlow Core Controller.
 * <p>
 * This service allows applications and controller modules to:
 * <ul>
 *     <li>
 *         register as datapath listeners, to be notified when OpenFlow
 *         datapaths connect to, or disconnect from, the controller
 *     </li>
 *     <li>
 *         register as message listeners, to be notified when OpenFlow
 *         messages sent from connected datapaths are received
 *         by the controller
 *     </li>
 *     <li>
 *         request information about the OpenFlow datapaths currently
 *         connected to the controller
 *     </li>
 *     <li>
 *         instruct the controller to send an OpenFlow message to an
 *         OpenFlow datapath
 *     </li>
 * </ul>
 * <p>
 * See also {@link ControllerService} for higher level abstractions.
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Thomas Vachuska
 */
public interface ListenerService {

    /** 
     * Adds the specified datapath listener to the controller.
     *
     * @param listener the listener to be added
     * @throws NullPointerException if listener is null
     */
    void addDataPathListener(DataPathListener listener);

    /** 
     * Removes the specified datapath listener from the controller.
     *
     * @param listener the listener to be removed
     * @throws NullPointerException if listener is null
     */
    void removeDataPathListener(DataPathListener listener);

    /** 
     * Adds the specified message listener to the controller.
     * <p>
     * The {@code types} argument specifies the types of message that the
     * listener cares about. When the controller receives a message,
     * it will be forwarded to the listener if and only if the
     * message's type is a member of the specified set.
     * <p>
     * An empty set (or null) is taken to mean that the listener
     * wants to hear about <em>every</em> message, regardless of type.
     * <p>
     * If a listener calls this method when it is already registered,
     * the specified set of message types <em>completely replaces</em>
     * the original set.
     *
     * @param listener the listener to be added
     * @param types the message types the listener cares about
     * @throws NullPointerException if listener is null
     */
    void addMessageListener(MessageListener listener,
                            Set<MessageType> types);

    /** 
     * Removes the specified message listener from the controller.
     *
     * @param listener the listener to be removed
     * @throws NullPointerException if listener is null
     */
    void removeMessageListener(MessageListener listener);

    /** 
     * Returns information describing each of the OpenFlow datapaths
     * currently connected to the controller.
     *
     * @return information describing all connected datapaths
     */
    Set<DataPathInfo> getAllDataPathInfo();

    /** 
     * Returns information describing a specific OpenFlow datapath.
     * A {@code NotFoundException} will be thrown if there is no datapath
     * connected to the controller with the given ID.
     *
     * @param dpid the datapath id
     * @return information describing the corresponding datapath
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist
     */
    DataPathInfo getDataPathInfo(DataPathId dpid);

    /** 
     * Returns the (negotiated) protocol version of the specified datapath.
     * A {@code NotFoundException} will be thrown if there is no datapath
     * connected to the controller with the given ID.
     *
     * @param dpid the datapath ID
     * @return the negotiated protocol version
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist
     */
    ProtocolVersion versionOf(DataPathId dpid);

    /** 
     * Returns statistical information about the controller.
     *
     * @return controller statistics
     */
    ControllerStats getStats();

    /**
     * Returns the port statistics from the specified datapath.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until all
     * the port statistics replies have returned from the datapath.
     *
     * @param dpid the target datapath
     * @return the list of port statistics
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist
     */
    List<MBodyPortStats> getPortStats(DataPathId dpid);

    /**
     * Returns the port statistics for the given port, from the specified
     * datapath.
     * <p>
     * Note that this is a <em>synchronous</em> call that blocks until
     * the port statistics reply has returned from the datapath.
     *
     * @param dpid the target datapath
     * @param portNumber the target port
     * @return the port statistics
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist or
     *          the specified port does not exist on the datapath
     */
    MBodyPortStats getPortStats(DataPathId dpid, BigPortNumber portNumber);

    /**
     * Instructs the controller to enable or disable the given port on the
     * specified datapath.
     * A message future is returned, to allow the caller to discover the
     * outcome of the request once it has been satisfied.
     *
     * @param dpid the target datapath
     * @param port the target port
     * @param enable true to enable; false to disable
     * @return a message future
     * @throws NullPointerException if either parameter is null
     * @throws NotFoundException if the specified datapath does not exist or
     *          the specified port does not exist on the datapath
     */
    MessageFuture enablePort(DataPathId dpid, BigPortNumber port,
                             boolean enable);

    /** 
     * Instructs the controller to send the given message to the specified
     * datapath, on the specified auxiliary channel. This is a privileged
     * method that should <em>only</em> be called by the packet sequencer.
     *
     * @param msg the message to send
     * @param dpid the target datapath
     * @param auxId the ID of the required auxiliary connection
     * @throws OpenflowException if there was a problem encoding or sending
     *          the message
     */
    void send(OpenflowMessage msg, DataPathId dpid, int auxId)
            throws OpenflowException;

    /** 
     * Instructs the controller to send the specified message to the
     * specified datapath on the main channel.
     *
     * @param msg the OpenFlow message to send
     * @param dpid the OpenFlow datapath to which the message is to be sent
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if msg is mutable
     * @throws OpenflowException if there was a problem encoding or sending
     *              the message
     */
    void send(OpenflowMessage msg, DataPathId dpid) throws OpenflowException;

    /** 
     * Instructs the controller to send the specified list of messages
     * (in order) to the specified datapath on the main channel.
     *
     * @param msgs the OpenFlow messages to send
     * @param dpid the OpenFlow datapath to which the messages are to be sent
     * @throws NullPointerException if either parameter is null, or if any
     *          element in the list is null
     * @throws IllegalArgumentException if any message in the list is mutable
     * @throws OpenflowException if there was a problem encoding or sending
     *              the messages
     */
    void send(List<OpenflowMessage> msgs, DataPathId dpid)
            throws OpenflowException;

    /**
     * Attempts to cache the message future based on its datapath ID and
     * request transfer ID. If successful, the given messages are sent to the
     * main connection. It is assumed that the future is associated with the
     * last message.  If an exception occurs, the future will be removed from
     * the cache and satisfied as a failure.
     *  
     * @param f the message future to add
     * @param msgs the array of 1 or more messages to send
     * @throws OpenflowException if no data path info exists for the
     *      datapath ID; or if a pending future already exists for the given
     *      datapath ID and transfer ID
     */
    void sendFuture(DataPathMessageFuture f, OpenflowMessage... msgs)
            throws OpenflowException;
    
    /**
     * Attempts to cache the message future based on its datapath ID and
     * request transfer ID. If successful, the associated message is sent to
     * the main connection. If an exception occurs, the future will be removed
     * from the cache and satisfied as a failure.
     *
     * @param f the message future to process
     * @throws OpenflowException if no data path info exists for the
     *      datapath ID; or if a pending future already exists for the given
     *      datapath ID and XID
     */
    void sendFuture(DataPathMessageFuture f)
            throws OpenflowException;

    /**
     * Finds the message future based on the messages's transfer ID and
     * specified datapath ID.
     *  
     * @param msg the message containing the transfer ID
     * @param dpid the id of the datapath
     * @return the datapath message future or null if no future was found
     */
    DataPathMessageFuture findFuture(OpenflowMessage msg, DataPathId dpid);
    
    /**
     * Removes the message future from the cache based on its datapath ID and
     * request transfer ID. The future is satisfied as a failure using the 
     * given cause.
     *  
     * @param f the message future to remove and satisfy
     * @param cause the reason for the future failure
     */
    void failFuture(DataPathMessageFuture f, Throwable cause);

    /**
     * Removes the message future from the cache based on its datapath ID and
     * request transfer ID. The future is satisfied as a failure using the 
     * given error message.
     *  
     * @param f the message future to remove and satisfy
     * @param msg the failure error message
     */
    void failFuture(DataPathMessageFuture f, OfmError msg);    
    
    /**
     * Removes the message future from the cache based on its datapath ID and
     * request transfer ID. The future is satisfied as a success using the 
     * given message.
     *  
     * @param f the message future to remove and satisfy
     * @param msg the message response that satisfies the future
     */
    void successFuture(DataPathMessageFuture f, OpenflowMessage msg);

    /**
     * Removes the message future from the cache based on its datapath ID and
     * transfer ID. The future is left unsatisfied.
     *
     * @param f the message future to cancel
     */
    void cancelFuture(DataPathMessageFuture f);

    /** 
     * Increments the count of dropped packets, and the number of bytes
     * from the associated network packet.
     *
     * @param totalLen the frame length reported by the packet-in message
     */
    void countDrop(int totalLen);

    /**
     * Returns the list of table features arrays cached by the core controller
     * during the extended handshake sequence, for the specified datapath.
     * 
     * @param dpid the target datapath
     * @return the list of table features arrays
     */
    List<MBodyTableFeatures.Array> getCachedTableFeatures(DataPathId dpid);

    /**
     * Returns the device description cached by the core controller during
     * the extended handshake sequence, for the specified datapath.
     * 
     * @param dpid the target datapath
     * @return the device description
     */
    MBodyDesc getCachedDeviceDesc(DataPathId dpid);
}
