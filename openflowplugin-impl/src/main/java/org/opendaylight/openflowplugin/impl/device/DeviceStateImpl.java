/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.device
 * <p/>
 * DeviceState is builded from {@link FeaturesReply} and {@link NodeId}. Both values are inside
 * {@link org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext}
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *         <p/>
 *         Created: Mar 29, 2015
 */
class DeviceStateImpl implements DeviceState {

    private final GetFeaturesOutput featuresOutput;
    private final Map<Long, PortGrouping> portGrouping;
    private final Map<Long, Long> portsBandwidth;
    private final NodeId nodeId;
    private final KeyedInstanceIdentifier<Node, NodeKey> nodeII;
    private final short version;
    private boolean valid;

    public DeviceStateImpl(@CheckForNull final FeaturesReply featuresReply, @Nonnull final NodeId nodeId) {
        Preconditions.checkArgument(featuresReply != null);
        Preconditions.checkArgument(featuresReply.getPhyPort() != null);
        featuresOutput = new GetFeaturesOutputBuilder(featuresReply).build();
        this.nodeId = Preconditions.checkNotNull(nodeId);
        nodeII = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));
        version = featuresReply.getVersion();
        portGrouping = new HashMap<>();
        portsBandwidth = new HashMap<>();
        for (final PhyPort port : featuresReply.getPhyPort()) {
            portGrouping.put(port.getPortNo(), port);
            portsBandwidth.put(port.getPortNo(), port.getMaxSpeed());
        }
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public KeyedInstanceIdentifier<Node, NodeKey> getNodeInstanceIdentifier() {
        return nodeII;
    }

    @Override
    public GetFeaturesOutput getFeatures() {
        return featuresOutput;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void setValid(final boolean valid) {
        this.valid = valid;
    }

    @Override
    public Map<Long, PortGrouping> getPhysicalPorts() {
        return portGrouping;
    }

    @Override
    public Map<Long, Long> getPortsBandwidth() {
        return portsBandwidth;
    }

    @Override
    public Set<Long> getPorts() {
        return portGrouping.keySet();
    }

    @Override
    public PortGrouping getPhysicalPort(final Long portNumber) {
        return portGrouping.get(portNumber);
    }

    @Override
    public Long getPortBandwidth(final Long portNumber) {
        return portsBandwidth.get(portNumber);
    }

    @Override
    public boolean isPortEnabled(final long portNumber) {
        return portGrouping.containsKey(portNumber);
    }

    @Override
    public boolean isPortEnabled(final PortGrouping port) {
        return portGrouping.containsValue(port);
    }

    @Override
    public List<PortGrouping> getEnabledPorts() {
        return new ArrayList<PortGrouping>(portGrouping.values());
    }

    @Override
    public int getSeed() {
        return hashCode();
    }

    @Override
    public short getVersion() {
        return version;
    }

}
