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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

@NonNullByDefault
public final class FlowGroupInfo implements Immutable {
    private final Instant time = Instant.now();
    private final FlowGroupStatus status;
    private final String description;
    private final String id;

    public FlowGroupInfo(final String id, final String description, final FlowGroupStatus status) {
        this.id = requireNonNull(id);
        this.description = requireNonNull(description);
        this.status = requireNonNull(status);
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public FlowGroupStatus getStatus() {
        return status;
    }

    public LocalDateTime getTime() {
        return LocalDateTime.ofInstant(time, ZoneOffset.UTC);
    }
}
