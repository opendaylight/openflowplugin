/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import com.google.common.base.Preconditions;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
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
    private final NodeId nodeId;
    private final KeyedInstanceIdentifier<Node, NodeKey> nodeII;
    private final short version;
    private volatile boolean valid;
    private boolean meterIsAvailable;
    private boolean groupIsAvailable;
    private boolean deviceSynchronized;
    private boolean flowStatisticsAvailable;
    private boolean tableStatisticsAvailable;
    private boolean portStatisticsAvailable;
    private boolean queueStatisticsAvailable;
    private volatile OfpRole role;
    private volatile boolean statPollEnabled;

    public DeviceStateImpl(@CheckForNull final FeaturesReply featuresReply, @Nonnull final NodeId nodeId) {
        Preconditions.checkArgument(featuresReply != null);
        featuresOutput = new GetFeaturesOutputBuilder(featuresReply).build();
        this.nodeId = Preconditions.checkNotNull(nodeId);
        nodeII = DeviceStateUtil.createNodeInstanceIdentifier(nodeId);
        version = featuresReply.getVersion();
        statPollEnabled = false;
        deviceSynchronized = false;
        role = OfpRole.BECOMESLAVE;
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
    public short getVersion() {
        return version;
    }

    @Override
    public boolean isMetersAvailable() {
        return meterIsAvailable;
    }

    @Override
    public void setMeterAvailable(final boolean available) {
        meterIsAvailable = available;
    }

    @Override
    public boolean isGroupAvailable() {
        return groupIsAvailable;
    }

    @Override
    public void setGroupAvailable(final boolean available) {
        groupIsAvailable = available;
    }

    @Override
    public boolean deviceSynchronized() {
        return deviceSynchronized;
    }

    @Override
    public boolean isFlowStatisticsAvailable() {
        return flowStatisticsAvailable;
    }

    @Override
    public void setFlowStatisticsAvailable(final boolean available) {
        flowStatisticsAvailable = available;
    }

    @Override
    public boolean isTableStatisticsAvailable() {
        return tableStatisticsAvailable;
    }

    @Override
    public void setTableStatisticsAvailable(final boolean available) {
        tableStatisticsAvailable = available;
    }

    @Override
    public boolean isPortStatisticsAvailable() {
        return portStatisticsAvailable;
    }

    @Override
    public void setPortStatisticsAvailable(final boolean available) {
        portStatisticsAvailable = available;
    }

    @Override
    public boolean isQueueStatisticsAvailable() {
        return queueStatisticsAvailable;
    }

    @Override
    public void setQueueStatisticsAvailable(final boolean available) {
        queueStatisticsAvailable = available;

    }

    @Override
    public void setDeviceSynchronized(final boolean _deviceSynchronized) {
        deviceSynchronized = _deviceSynchronized;
    }

    @Override
    public OfpRole getRole() {
        return role;
    }

    @Override
    public void setRole(final OfpRole role) {
        this.role = role;
    }

    @Override
    public boolean isStatisticsPollingEnabled() {
        return statPollEnabled;
    }

    @Override
    public void setStatisticsPollingEnabledProp(final boolean statPollEnabled) {
        this.statPollEnabled = statPollEnabled;
    }
}
