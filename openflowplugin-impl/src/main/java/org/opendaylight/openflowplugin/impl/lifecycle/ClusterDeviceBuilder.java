/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ClusterDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

public final class ClusterDeviceBuilder {

    private final NodeId nodeId;
    private boolean connected;
    private DeviceInfo deviceInfo;

    ClusterDeviceBuilder(@Nonnull final NodeId nodeId) {
        this.nodeId = nodeId;
        deviceInfo = null;
    }

    public ClusterDeviceBuilder setDeviceInfo(@Nonnull final DeviceInfo deviceInfo) {
        if (deviceInfo.getNodeId().equals(this.nodeId)) {
            this.deviceInfo = deviceInfo;
        }
        return this;
    }

    public ClusterDeviceBuilder setConnected(final boolean connected) {
        this.connected = connected;
        return this;
    }

    public ClusterDevice build() {
        return new ClusterDeviceImpl(this);
    }

    private final class ClusterDeviceImpl implements ClusterDevice {

        private final NodeId nodeId;
        private DeviceInfo deviceInfo;
        private boolean connected;

        ClusterDeviceImpl(ClusterDeviceBuilder clusterDeviceBuilder) {
            this.nodeId = clusterDeviceBuilder.nodeId;
            this.deviceInfo = clusterDeviceBuilder.deviceInfo;
            this.connected = clusterDeviceBuilder.connected;
        }

        @Override
        public DeviceInfo changeDeviceInfo(@Nonnull DeviceInfo deviceInfo) {
            if (deviceInfo.getNodeId().equals(this.nodeId)) {
                this.deviceInfo = deviceInfo;
            }
            return this.deviceInfo;
        }

        @Nullable
        @Override
        public DeviceInfo getDeviceInfo() {
            return this.deviceInfo;
        }

        @Override
        public boolean isConnected() {
            return this.connected;
        }

        @Override
        public void connect() {
            this.connected = true;
        }

        @Override
        public void disconnect() {
            this.connected = false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClusterDeviceImpl that = (ClusterDeviceImpl) o;

            return nodeId != null ? nodeId.equals(that.nodeId) : that.nodeId == null;
        }

        @Override
        public int hashCode() {
            return nodeId != null ? nodeId.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "ClusterDeviceImpl{" +
                    "nodeId=" + nodeId +
                    ", deviceInfo=" + deviceInfo +
                    ", connected=" + connected +
                    '}';
        }
    }

}
