/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;
import java.math.BigInteger;
import java.util.Objects;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowHash;
import org.opendaylight.openflowplugin.impl.util.HashUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public class FlowHashFactory {


    public FlowHashFactory() {
    }

    public static FlowHash create(Flow flow, short version) {
        long hash = calculateHash(flow, version);
        return new FlowHashDto(hash, flow);
    }

    private static long calculateHash(Flow flow, short version) {
        return HashUtil.calculateMatchHash(flow.getMatch(), version);
    }


    private static final class FlowHashDto implements FlowHash {

        private final long flowHash;
        private final int intHashCode;

        private final short tableId;
        private final int priority;
        private final BigInteger cookie;

        public FlowHashDto(final long flowHash, final Flow flow) {
            this.flowHash = flowHash;
            this.intHashCode = Longs.hashCode(flowHash);
            tableId = Preconditions.checkNotNull(flow.getTableId(), "flow tableId must not be null");
            priority = Preconditions.checkNotNull(flow.getPriority(), "flow priority must not be null");
            cookie = MoreObjects.firstNonNull(flow.getCookie(), OFConstants.DEFAULT_FLOW_COOKIE).getValue();
        }


        @Override
        public int hashCode() {
            return intHashCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (null == obj) {
                return false;
            }
            if (!(obj instanceof FlowHash)) {
                return false;
            }
            FlowHash that = (FlowHash) obj;
            if (this.flowHash == that.getFlowHash()
                    && this.tableId == that.getTableId()
                    && this.priority == that.getPriority()
                    && Objects.equals(this.cookie, that.getCookie())) {
                return true;
            }
            return false;
        }

        @Override
        public long getFlowHash() {
            return flowHash;
        }

        @Override
        public short getTableId() {
            return tableId;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public BigInteger getCookie() {
            return cookie;
        }
    }
}
