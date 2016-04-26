/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Holder for items to be pushed to device.
 * Contains two sets of groups -set of items to be pushed and set of tuples for update.
 */
public class ItemSyncBox<I> {

    private Set<I> itemsToAdd = new HashSet<>();
    private Set<ItemUpdateTuple<I>> itemsToUpdate = new HashSet<>();

    public Set<I> getItemsToAdd() {
        return itemsToAdd;
    }

    public Set<ItemUpdateTuple<I>> getItemsToUpdate() {
        return itemsToUpdate;
    }

    public boolean isEmpty() {
        return itemsToAdd.isEmpty() && itemsToUpdate.isEmpty();
    }

    /**
     * Tuple holder for original and updated item
     *
     * @param <I> basic type
     */
    public static final class ItemUpdateTuple<I> {
        private final I original;
        private final I updated;

        public ItemUpdateTuple(I original, I updated) {
            this.original = original;
            this.updated = updated;
        }

        public I getOriginal() {
            return original;
        }

        public I getUpdated() {
            return updated;
        }
    }
}
