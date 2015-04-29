/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import com.google.common.primitives.Longs;
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
        return new FlowHashDto(hash);
    }

    private static long calculateHash(Flow flow, DeviceContext deviceContext) {
        long hash = HashUtil.calculateMatchHash(flow.getMatch(), deviceContext);
        hash += flow.getPriority();
        hash += flow.getTableId();
        hash += flow.getCookie().hashCode();
        return hash;
    }


    private static final class FlowHashDto implements FlowHash {

        private final long flowHash;
        private final int intHashCode;

        public FlowHashDto(final long flowHash) {

            this.flowHash = flowHash;
            this.intHashCode = Longs.hashCode(flowHash);
        }


        @Override
        public int hashCode(){
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
            if (this.flowHash == ((FlowHash) obj).getFlowHash()) {
                return true;
            }
            return false;
        }

        @Override
        public long getFlowHash() {
            return flowHash;
        }
    }
}
