/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

import org.opendaylight.of.controller.flow.FlowListener;
import org.opendaylight.of.controller.flow.GroupListener;
import org.opendaylight.of.controller.flow.MeterListener;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.controller.pkt.SequencedPacketListener;
import org.opendaylight.of.controller.pkt.SequencedPacketListenerRole;
import org.opendaylight.of.controller.pkt.SplMetric;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.dt.*;
import org.opendaylight.of.lib.mp.*;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.api.NotFoundException;
import org.opendaylight.util.api.auth.AuthorizationException;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.packet.ProtocolId;

import java.util.List;
import java.util.Set;

/**
 * Provides the public API to the SDN OpenFlow Controller &mdash; a
 * one-stop-shop for SDN applications interacting with the controller.
 * <p>
 * This service allows applications to:
 * <ul>
 *     <li>
 *         register as packet listeners, to participate in the processing
 *         of <em>Packet-In</em> messages
 *     </li>
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
 *     <li>
 *         register as flow listeners, to request information about flows
 *         installed on a given datapath
 *     </li>
 *     <li>
 *         instruct the controller to send flows through the Flow Tracker
 *     </li>
 *     <li>
 *     	   request information about the pipeline definition for a given 
 *     	   datapath
 *     </li>
 * </ul>
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Thomas Vachuska
 * @author Radhika Hegde
 * @author Shruthy Mohanram
 */
public interface ControllerService {

    // =================================================================
    // === Packet Sequencer

    /** 
     * Adds the specified packet listener to the packet sequencer, in the
     * specified role and at the specified altitude.
     * Higher altitudes come earlier in the sequence than lower altitudes
     * (within the specific role).
     * Note that no two listeners (in the same role) may register with
     * the same altitude; if this is attempted an exception will be thrown.
     * <p>
     * The listener's {@link SequencedPacketListener#event event()} callback
     * will be invoked for every <em>Packet-In</em> message that the controller
     * passes to the packet sequencer.

     * @see #addPacketListener(SequencedPacketListener, org.opendaylight.of.controller.pkt.SequencedPacketListenerRole, int, Set)
     *
     * @param listener the listener to be added
     * @param role the role the listener wishes to assume
     * @param altitude the listener's altitude
     * @throws NullPointerException if listener or role is null
     * @throws IllegalArgumentException if altitude is negative
     * @throws IllegalStateException if the altitude (for the role)
     *          has already been claimed
     */
    void addPacketListener(SequencedPacketListener listener,
                           SequencedPacketListenerRole role, int altitude);

    /** 
     * Adds the specified packet listener to the sequencer, in the
     * specified role and at the specified altitude.
     * Higher altitudes come earlier in the sequence than lower altitudes
     * (within the specific role).
     * Note that no two listeners (in the same role) may register with
     * the same altitude; if this is attempted an exception will be thrown.
     * <p>
     * The {@code interest} argument specifies the protocols that the listener
     * cares about. When the sequencer receives a <em>Packet-In</em> message,
     * it will be forwarded to the listener if the packet contains any protocol
     * that is a member of the specified set.
     *
     * @see #addPacketListener(SequencedPacketListener, org.opendaylight.of.controller.pkt.SequencedPacketListenerRole, int)
     *
     * @param listener the listener to be added
     * @param role the role the listener wishes to assume
     * @param altitude the listener's altitude
     * @param interest the packet protocols the listener is interested in
     * @throws NullPointerException if listener or role is null
     * @throws IllegalArgumentException if altitude is negative
     * @throws IllegalStateException if the altitude (for the role)
     *      has already been claimed
     */
    void addPacketListener(SequencedPacketListener listener,
                           SequencedPacketListenerRole role, int altitude,
                           Set<ProtocolId> interest);

    /** 
     * Removes the specified packet listener from the packet sequencer.
     *
     * @param listener the listener to be removed
     * @throws NullPointerException if listener is null
     */
    void removePacketListener(SequencedPacketListener listener);

    /**
     * Returns a snapshot of the metrics collected for the registered
     * {@link SequencedPacketListener sequenced packet listeners}. Note that
     * the list is returned in the same order that the packet listeners get
     * to see and process the packets.
     *
     * @return the packet listener metrics
     */
    List<SplMetric> getSplMetrics();

    // =================================================================
    // === Message Listeners

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
     * However, <em>PacketIn</em> messages are not forwarded in this case;
     * see note below.
     * <p>
     * If a listener calls this method when it is already registered,
     * the specified set of message types <em>completely replaces</em>
     * the original set.
     * <p>
     * <strong>Important Note:</strong><br/>
     * <em>PacketIn</em> messages are processed exclusively by the packet
     * sequencer. To participate in <em>PacketIn</em> processing, you must use
     * {@link #addPacketListener(SequencedPacketListener, org.opendaylight.of.controller.pkt.SequencedPacketListenerRole, int, Set)}.
     * Attempting to request <em>PacketIn</em> messages by including
     * {@code PACKET_IN} in the {@code types} set will throw an exception.
     *
     * @param listener the listener to be added
     * @param types the message types the listener cares about
     * @throws NullPointerException if listener is null
     * @throws IllegalArgumentException types includes PACKET_IN
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


    // =================================================================
    // === Datapath Listeners and Information

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

    // =================================================================
    // === Sending Messages

    /** 
     * Instructs the controller to send the specified message to the
     * specified datapath.
     * A message future is returned, to allow the caller to discover the
     * outcome of the request once it has been satisfied.
     * <p>
     * <strong>Important Note:</strong><br/>
     * The sending of <em>FlowMod</em> messages via this method is disallowed;
     * use {@link #sendFlowMod} instead.
     *
     * @param msg the OpenFlow message to send
     * @param dpid the OpenFlow datapath to which the message is to be sent
     * @return a message future
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if msg is mutable, or if it is a
     *          <em>FlowMod</em>
     * @throws OpenflowException if there was a problem encoding or sending
     *              the message
     */
    MessageFuture send(OpenflowMessage msg, DataPathId dpid)
            throws OpenflowException;

    /** 
     * Instructs the controller to send the specified list of messages
     * (in order) to the specified datapath.
     * A list of message futures (one for each message) is returned, to allow
     * the caller to discover the outcome of the requests once they have
     * been satisfied.
     * <p>
     * <strong>Important Note:</strong><br/>
     * The sending of <em>FlowMod</em> messages via this method is disallowed;
     * use {@link #sendFlowMod} instead.
     *
     * @param msgs the OpenFlow messages to send
     * @param dpid the OpenFlow datapath to which the messages are to be sent
     * @return a list of message futures
     * @throws NullPointerException if either parameter is null, or if any
     *          element in the list is null
     * @throws IllegalArgumentException if any message in the list is mutable,
     *          or is a <em>FlowMod</em>
     * @throws OpenflowException if there was a problem encoding or sending
     *              the messages
     */
    List<MessageFuture> send(List<OpenflowMessage> msgs, DataPathId dpid)
            throws OpenflowException;


    // =================================================================
    // === Management Interface

    /**
     * Returns an instance of the management interface.
     *
     * @return a handle on the management interface
     * @throws AuthorizationException if the caller is not authorized
     */
    ControllerMx getControllerMx();


    // =================================================================
    // === Flow Tracker

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
     * Registers an initial flow contributor with the controller.
     * When new datapaths connect to the controller, each registered
     * contributor will be asked to supply any flow mods they wish to be
     * pushed to the newly connected datapath.
     *
     * @param ifc the flow contributor to register
     * @throws NullPointerException if ifc is null
     */
    void registerInitialFlowContributor(InitialFlowContributor ifc);

    /**
     * Cancels the given flow contributor registration.
     *
     * @param ifc the flow contributor to unregister
     * @throws NullPointerException if ifc is null
     */
    void unregisterInitialFlowContributor(InitialFlowContributor ifc);

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

    /**
     * Adds the specified flow listener to the controller.
     *
     * @param listener the flow listener to be added
     * @throws NullPointerException if listener is null
     */
    void addFlowListener(FlowListener listener);

    /**
     * Removes the specified flow listener from the controller.
     *
     * @param listener the flow listener to be removed
     * @throws NullPointerException if listener is null
     */
    void removeFlowListener(FlowListener listener);

    /** 
     * Returns the {@link PipelineDefinition} for the given datapath. 
     *
     * @param dpid the target datapath
     * @return the pipeline definition for the given datapath
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist
     */
    PipelineDefinition getPipelineDefinition(DataPathId dpid);

    // =================================================================
    // === Group Tracker

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

    /**
     * Adds the specified group listener to the controller.
     *
     * @param listener the group listener to be added
     * @throws NullPointerException if listener is null
     */
    void addGroupListener(GroupListener listener);

    /**
     * Removes the specified group listener from the controller.
     *
     * @param listener the group listener to be removed
     * @throws NullPointerException if listener is null
     */
    void removeGroupListener(GroupListener listener);


    // =================================================================
    // === Meter Tracker


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
     * @see MeterModCommand
     */
    MessageFuture sendMeterMod(OfmMeterMod meterMod, DataPathId dpid)
            throws OpenflowException;

    /**
     * Adds the specified meter listener to the controller.
     *
     * @param listener the meter listener to be added
     * @throws NullPointerException if listener is null
     */
    void addMeterListener(MeterListener listener);

    /**
     * Removes the specified meter listener from the controller.
     *
     * @param listener the meter listener to be removed
     * @throws NullPointerException if listener is null
     */
    void removeMeterListener(MeterListener listener);


    // =================================================================
    // === State information

    /**
     * Indicates whether or not the controller is operating in hybrid mode,
     * which is where it will defer primary forwarding decisions to the
     * connected switch. Various network services or applications should
     * consult this and appropriately tailor their behaviours.
     *
     * @return true if hybrid mode is enabled
     */
    boolean isHybridMode();

}
