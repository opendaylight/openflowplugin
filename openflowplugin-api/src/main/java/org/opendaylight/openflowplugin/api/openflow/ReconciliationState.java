/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import org.eclipse.jdt.annotation.NonNull;

// FIXME: this should be a proper JMX type
public class ReconciliationState {
    public enum ReconciliationStatus {
        STARTED,
        COMPLETED,
        FAILED
    }

    private ReconciliationStatus status;
    private LocalDateTime time;

    public ReconciliationState(final @NonNull ReconciliationStatus status) {
        this.status = requireNonNull(status);
        this.time = LocalDateTime.now();
    }

    public ReconciliationStatus getState() {
        return status;
    }

    public void setStatus(final ReconciliationStatus status) {
        this.status = status;
        this.time = LocalDateTime.now();
    }

    @Override
    public String toString() {
        // FIXME: expose String formatting separately
        return String.format("%-25s %-25s", this.status, this.time);
    }
}

