/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.flow.registry;

import org.opendaylight.openflowplugin.api.openflow.flow.registry.FlowHash;
import org.opendaylight.openflowplugin.impl.util.HashUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public class FlowHashFactory {


    public FlowHashFactory() {
    }

    public static FlowHash create(Flow flow) {
        int hash = calculateHash(flow);
        return new FlowHashDto(hash);
    }

    private static int calculateHash(Flow flow) {
        int hash = 0;
        Match match = flow.getMatch();
        hash = HashUtil.calculateMatchHash(match);
        hash += flow.getPriority();
        hash += flow.getTableId();
        hash += flow.getCookie().hashCode();
        return hash;
    }


    private static final class FlowHashDto implements FlowHash {

        private final int hashCode;

        public FlowHashDto(final int hashCode) {
            this.hashCode = hashCode;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (null == obj) {
                return false;
            }
            if (!(obj instanceof FlowHash)) {
                return false;
            }
            if (this.hashCode == obj.hashCode()) {
                return true;
            }
            return false;
        }


    }
}
