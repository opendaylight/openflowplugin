/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.util;

/**
 * General placeholder for add/update/remove counts.
 */
public class CrudCounts {
    private int added;
    private int updated;
    private int removed;

    public int getAdded() {
        return added;
    }

    public void setAdded(final int added) {
        this.added = added;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(final int updated) {
        this.updated = updated;
    }

    public int getRemoved() {
        return removed;
    }

    public void setRemoved(final int removed) {
        this.removed = removed;
    }

    public void incAdded() {
        added++;
    }

    public void incUpdated() {
        updated++;
    }

    public void incRemoved() {
        removed++;
    }

    public void decAdded() {
        added--;
    }

    public void decUpdated() {
        updated--;
    }

    public void decRemoved() {
        removed--;
    }
}
