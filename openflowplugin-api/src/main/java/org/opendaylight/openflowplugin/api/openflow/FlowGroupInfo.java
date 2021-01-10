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
import org.opendaylight.yangtools.concepts.Immutable;

@NonNullByDefault
public abstract class FlowGroupInfo implements Immutable {
    private final Instant time = Instant.now();
    private final FlowGroupStatus status;

    protected FlowGroupInfo(final FlowGroupStatus status) {
        this.status = requireNonNull(status);
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
