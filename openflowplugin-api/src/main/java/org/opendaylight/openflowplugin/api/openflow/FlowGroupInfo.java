/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.Uint8;

@NonNullByDefault
public abstract class FlowGroupInfo implements Immutable {
    private static final class Flow extends FlowGroupInfo {
        private final Uint8 tableId;
        private final FlowId id;

        Flow(final FlowGroupStatus status, final FlowId id, final Uint8 tableId) {
            super(status);
            this.id = requireNonNull(id);
            this.tableId = requireNonNull(tableId);
        }

        @Override
        public String getId() {
            return id.getValue();
        }

        @Override
        public String getDescription() {
            return tableId.toString();
        }
    }

    private static final class Group extends FlowGroupInfo {
        private final GroupTypes type;
        private final GroupId id;

        Group(final FlowGroupStatus status, final GroupId id, final GroupTypes type) {
            super(status);
            this.id = requireNonNull(id);
            this.type = requireNonNull(type);
        }

        @Override
        public String getId() {
            // FIXME: GroupId.toString() is not pretty, can we do something else?
            return id.toString();
        }

        @Override
        public String getDescription() {
            return type.getName();
        }
    }

    private final Instant time = Instant.now();
    private final FlowGroupStatus status;

    private FlowGroupInfo(final FlowGroupStatus status) {
        this.status = requireNonNull(status);
    }

    public static FlowGroupInfo ofFlow(final FlowId id, final Uint8 tableId, final FlowGroupStatus status) {
        return new Flow(status, id, tableId);
    }

    public static FlowGroupInfo ofGroup(final GroupId id, final GroupTypes type, final FlowGroupStatus status) {
        return new Group(status, id, type);
    }

    public abstract String getId();

    public abstract String getDescription();

    public final FlowGroupStatus getStatus() {
        return status;
    }

    public final Instant getInstantUTC() {
        return time;
    }
}
