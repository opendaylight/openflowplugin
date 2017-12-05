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
 * A source of switch item lifecycle enables for injecting of
 * a {@link org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener}
 * in order to act upon item lifecycle changes.
 */
public interface ItemLifeCycleSource {

    /**
     * @param itemLifecycleListener acts upon lifecycle changes
     */
    void setItemLifecycleListener(@Nullable ItemLifecycleListener itemLifecycleListener);


}
