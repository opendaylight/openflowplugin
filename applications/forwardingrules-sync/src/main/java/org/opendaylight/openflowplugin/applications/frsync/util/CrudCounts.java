package org.opendaylight.openflowplugin.applications.frsync.util;

/**
 * Purpose: general place holder for add/update/remove counts
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
}
