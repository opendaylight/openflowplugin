/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import java.time.LocalDateTime;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconciliationState {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationState.class);

    public enum ReconciliationStatus {
        STARTED,
        COMPLETED,
        FAILED
    }

    private ReconciliationStatus status;
    private LocalDateTime time;

    public ReconciliationState(@Nullable ReconciliationStatus status, @Nonnull LocalDateTime time) {
        this.status = status;
        this.time = time;
    }

    public ReconciliationStatus getState() {
        return status;
    }

    public void setState(ReconciliationStatus status, LocalDateTime time) {
        this.status = status;
        this.time = time;
    }

    @Override
    public String toString() {
        return String.format("%-25s %-25s", this.status, this.time);
    }
}

