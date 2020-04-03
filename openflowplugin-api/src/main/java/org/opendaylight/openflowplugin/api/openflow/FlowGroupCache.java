/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import java.time.LocalDateTime;

public class FlowGroupCache {
    private final String id;
    private final String description;
    private final FlowGroupStatus status;
    private final LocalDateTime time;

    public FlowGroupCache(String id, String description, FlowGroupStatus status,
                          LocalDateTime time) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.time = time;
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
        return time;
    }
}
