/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import com.google.common.primitives.Longs;
import java.math.BigInteger;
import java.util.Objects;
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

    public static FlowHash create(Flow flow, DeviceContext deviceContext) {
        long hash = calculateHash(flow, deviceContext);
        return new FlowHashDto(hash, flow);
    }

    private static long calculateHash(Flow flow, DeviceContext deviceContext) {
        return HashUtil.calculateMatchHash(flow.getMatch(), deviceContext);
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
            tableId = flow.getTableId();
            priority = flow.getPriority();
            cookie = flow.getCookie().getValue();
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
            return 0;
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public BigInteger getCookie() {
            return null;
        }
    }
}
