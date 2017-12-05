/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.rpc;

import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;

/**
 * provides read-write access to assigned {@link ItemLifecycleListener}
 */
public interface ItemLifeCycleKeeper extends ItemLifeCycleSource {

    /**
     * @return assigned item lifecycle listener
     */
    @Nullable
    ItemLifecycleListener getItemLifecycleListener();
}
