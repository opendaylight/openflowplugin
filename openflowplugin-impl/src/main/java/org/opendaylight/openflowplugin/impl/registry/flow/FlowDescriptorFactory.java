/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.registry.flow;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * This class serves as factory for creating
 * {@link org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor}.
 */
public final class FlowDescriptorFactory {

    private FlowDescriptorFactory() {
        // Hide implicit constructor
    }

    @NonNull
    public static FlowDescriptor create(final Uint8 tableId, @NonNull final FlowId flowId) {
        return new FlowDescriptorDto(new TableKey(tableId), requireNonNull(flowId));
    }

    private static final class FlowDescriptorDto implements FlowDescriptor {

        private final FlowId flowId;
        private final TableKey tableKey;

        private FlowDescriptorDto(@NonNull final TableKey tableKey, @NonNull final FlowId flowId) {
            this.flowId = flowId;
            this.tableKey = tableKey;
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            FlowDescriptorDto that = (FlowDescriptorDto) object;
            return Objects.equal(flowId, that.flowId) && Objects.equal(tableKey, that.tableKey);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(flowId, tableKey);
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