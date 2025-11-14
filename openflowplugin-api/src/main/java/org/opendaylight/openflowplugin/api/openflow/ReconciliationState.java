/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import java.time.LocalDateTime;
import org.eclipse.jdt.annotation.Nullable;

public class ReconciliationState {
    public enum ReconciliationStatus {
        STARTED,
        COMPLETED,
        FAILED
    }

    private ReconciliationStatus status;
    private LocalDateTime time;

    public ReconciliationState(@Nullable ReconciliationStatus status, LocalDateTime time) {
        this.status = status;
        this.time = time;
    }

    public ReconciliationStatus getState() {
        return status;
    }

    public void setState(ReconciliationStatus newStatus, LocalDateTime newTime) {
        this.status = newStatus;
        this.time = newTime;
    }

    @Override
    public String toString() {
        return String.format("%-25s %-25s", this.status, this.time);
    }
}

