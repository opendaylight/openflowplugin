/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.flow.registry;

import org.opendaylight.openflowplugin.api.openflow.flow.registry.FlowDescriptor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;

/**
 * This class serves as factory for creating {@link org.opendaylight.openflowplugin.api.openflow.flow.registry.FlowDescriptor}
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 9.4.2015.
 */
public class FlowDescriptorFactory {

    public static FlowDescriptor create(final short tableId, final FlowId fLowId) {
        final TableKey tableKey = new TableKey(tableId);
        return new FlowDescriptorDto(tableKey, fLowId);
    }

    private static final class FlowDescriptorDto implements FlowDescriptor {

        private final FlowId flowId;
        private final TableKey tableKey;

        public FlowDescriptorDto(final TableKey tableKey, final FlowId flowId) {
            this.flowId = flowId;
            this.tableKey = tableKey;
        }

        @Override
        public FlowId getFlowId() {
            return flowId;
        }

        @Override
        public TableKey getTableKey() {
            return tableKey;
        }
    }
}
