/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.common;

import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleKeeper;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;

/**
 * simple standalone {@link ItemLifeCycleSource} implementation
 */
public class ItemLifeCycleSourceImpl implements ItemLifeCycleKeeper {

    private ItemLifecycleListener itemLifecycleListener;

    @Override
    public void setItemLifecycleListener(@Nullable ItemLifecycleListener itemLifecycleListener) {

        this.itemLifecycleListener = itemLifecycleListener;
    }

    @Override
    public ItemLifecycleListener getItemLifecycleListener() {
        return itemLifecycleListener;
    }
}
