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
import org.opendaylight.of.lib.dt.*;
import org.opendaylight.of.lib.mp.*;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.packet.ProtocolId;

import java.util.List;
import java.util.Set;

/**
 * An adapter for the {@link ControllerService} API, provided specifically for
 * unit tests to use, to insulate themselves from changes in the API.
 *
 * @author Simon Hunt
 */
public class ControllerServiceAdapter implements ControllerService {
    @Override public void addPacketListener(SequencedPacketListener listener, SequencedPacketListenerRole role, int altitude) { }
    @Override public void addPacketListener(SequencedPacketListener listener, SequencedPacketListenerRole role, int altitude, Set<ProtocolId> interest) { }
    @Override public void removePacketListener(SequencedPacketListener listener) { }
    @Override public List<SplMetric> getSplMetrics() { return null; }

    @Override public void addMessageListener(MessageListener listener, Set<MessageType> types) { }
    @Override public void removeMessageListener(MessageListener listener) { }

    @Override public void addDataPathListener(DataPathListener listener) { }
    @Override public void removeDataPathListener(DataPathListener listener) { }
    @Override public Set<DataPathInfo> getAllDataPathInfo() { return null; }
    @Override public DataPathInfo getDataPathInfo(DataPathId dpid) { return null; }
    @Override public ProtocolVersion versionOf(DataPathId dpid) { return null; }
    @Override public ControllerStats getStats() { return null; }

    @Override public List<MBodyPortStats> getPortStats(DataPathId dpid) { return null; }
    @Override public MBodyPortStats getPortStats(DataPathId dpid, BigPortNumber portNumber) { return null; }
    @Override public MessageFuture enablePort(DataPathId dpid, BigPortNumber port, boolean enable) { return null; }

    @Override public MessageFuture send(OpenflowMessage msg, DataPathId dpid) throws OpenflowException { return null; }
    @Override public List<MessageFuture> send(List<OpenflowMessage> msgs, DataPathId dpid) throws OpenflowException { return null; }

    @Override public ControllerMx getControllerMx() { return null; }

    @Override public List<MBodyFlowStats> getFlowStats(DataPathId dpid, TableId tableId) { return null; }

    @Override public void registerInitialFlowContributor(InitialFlowContributor ifc) { }
    @Override public void unregisterInitialFlowContributor(InitialFlowContributor ifc) { }

    @Override public void sendFlowMod(OfmFlowMod flowMod, DataPathId dpid) throws OpenflowException { }
    @Override public MessageFuture sendConfirmedFlowMod(OfmFlowMod flowMod, DataPathId dpid) throws OpenflowException { return null; }
    @Override public void addFlowListener(FlowListener listener) { }
    @Override public void removeFlowListener(FlowListener listener) { }
    @Override public PipelineDefinition getPipelineDefinition(DataPathId dpId) { return null; }

    @Override public List<MBodyGroupDescStats> getGroupDescription(DataPathId dpid) { return null; }
    @Override public MBodyGroupDescStats getGroupDescription(DataPathId dpid, GroupId groupId) { return null; }
    @Override public List<MBodyGroupStats> getGroupStats(DataPathId dpid) { return null; }
    @Override public MBodyGroupStats getGroupStats(DataPathId dpid, GroupId groupId) { return null; }
    @Override public MBodyGroupFeatures getGroupFeatures(DataPathId dpid) { return null; }
    @Override public MessageFuture sendGroupMod(OfmGroupMod groupMod, DataPathId dpid) throws OpenflowException { return null; }
    @Override public void addGroupListener(GroupListener listener) { }
    @Override public void removeGroupListener(GroupListener listener) { }

    @Override public List<MBodyMeterConfig> getMeterConfig(DataPathId dpid) { return null; }
    @Override public MBodyMeterConfig getMeterConfig(DataPathId dpid, MeterId meterId) { return null; }
    @Override public List<MBodyMeterStats> getMeterStats(DataPathId dpid) { return null; }
    @Override public MBodyMeterStats getMeterStats(DataPathId dpid, MeterId meterId) { return null; }
    @Override public MBodyMeterFeatures getMeterFeatures(DataPathId dpid) { return null; }
    @Override public List<MBodyExperimenter> getExperimenter(DataPathId dpid) { return null; }
    @Override public MessageFuture sendMeterMod(OfmMeterMod meterMod, DataPathId dpid) throws OpenflowException { return null; }
    @Override public void addMeterListener(MeterListener listener) { }
    @Override public void removeMeterListener(MeterListener listener) { }

    @Override public boolean isHybridMode() { return false; }

}
