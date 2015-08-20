/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.tools;

/**
 * Class is designed as ConfigSubsitem settings holder
 */
public class OldNotifProviderConfig {

    private final boolean flowSupport;
    private final boolean meterSupport;
    private final boolean groupSupport;
    private final boolean nodeConnectorStatSupport;
    private final boolean flowTableStatSupport;
    private final boolean groupStatSupport;
    private final boolean meterStatSupport;
    private final boolean queueStatSupport;
    private final boolean flowStatSupport;

    private OldNotifProviderConfig(final OldNotifProviderConfigBuilder builder) {
        this.flowSupport = builder.isFlowSupport();
        this.meterSupport = builder.isMeterSupport();
        this.groupSupport = builder.isGroupSupport();
        this.nodeConnectorStatSupport = builder.isNodeConnectorStatSupport();
        this.flowTableStatSupport = builder.isFlowTableStatSupport();
        this.groupStatSupport = builder.isGroupStatSupport();
        this.meterStatSupport = builder.isMeterStatSupport();
        this.queueStatSupport = builder.isQueueStatSupport();
        this.flowStatSupport = builder.isFlowStatSupport();
    }

    public boolean isFlowSupport() {
        return flowSupport;
    }

    public boolean isMeterSupport() {
        return meterSupport;
    }

    public boolean isGroupSupport() {
        return groupSupport;
    }

    public boolean isNodeConnectorStatSupport() {
        return nodeConnectorStatSupport;
    }

    public boolean isFlowTableStatSupport() {
        return flowTableStatSupport;
    }

    public boolean isGroupStatSupport() {
        return groupStatSupport;
    }

    public boolean isMeterStatSupport() {
        return meterStatSupport;
    }

    public boolean isQueueStatSupport() {
        return queueStatSupport;
    }

    public boolean isFlowStatSupport() {
        return flowStatSupport;
    }

    public static OldNotifProviderConfigBuilder builder() {
        return new OldNotifProviderConfigBuilder();
    }

    public static class OldNotifProviderConfigBuilder {
        private boolean flowSupport;
        private boolean meterSupport;
        private boolean groupSupport;
        private boolean nodeConnectorStatSupport;
        private boolean flowTableStatSupport;
        private boolean groupStatSupport;
        private boolean meterStatSupport;
        private boolean queueStatSupport;
        private boolean flowStatSupport;

        public boolean isFlowSupport() {
            return flowSupport;
        }

        public void setFlowSupport(final boolean flowSupport) {
            this.flowSupport = flowSupport;
        }

        public boolean isMeterSupport() {
            return meterSupport;
        }

        public void setMeterSupport(final boolean meterSupport) {
            this.meterSupport = meterSupport;
        }

        public boolean isGroupSupport() {
            return groupSupport;
        }

        public void setGroupSupport(final boolean groupSupport) {
            this.groupSupport = groupSupport;
        }

        public boolean isNodeConnectorStatSupport() {
            return nodeConnectorStatSupport;
        }

        public void setNodeConnectorStatSupport(final boolean nodeConnectorStatSupport) {
            this.nodeConnectorStatSupport = nodeConnectorStatSupport;
        }

        public boolean isFlowTableStatSupport() {
            return flowTableStatSupport;
        }

        public void setFlowTableStatSupport(final boolean flowTableStatSupport) {
            this.flowTableStatSupport = flowTableStatSupport;
        }

        public boolean isGroupStatSupport() {
            return groupStatSupport;
        }

        public void setGroupStatSupport(final boolean groupStatSupport) {
            this.groupStatSupport = groupStatSupport;
        }

        public boolean isMeterStatSupport() {
            return meterStatSupport;
        }

        public void setMeterStatSupport(final boolean meterStatSupport) {
            this.meterStatSupport = meterStatSupport;
        }

        public boolean isQueueStatSupport() {
            return queueStatSupport;
        }

        public void setQueueStatSupport(final boolean queueStatSupport) {
            this.queueStatSupport = queueStatSupport;
        }

        public boolean isFlowStatSupport() {
            return flowStatSupport;
        }

        public void setFlowStatSupport(final boolean flowStatSupport) {
            this.flowStatSupport = flowStatSupport;
        }

        public OldNotifProviderConfig build() {
            return new OldNotifProviderConfig(this);
        }
    }
}

