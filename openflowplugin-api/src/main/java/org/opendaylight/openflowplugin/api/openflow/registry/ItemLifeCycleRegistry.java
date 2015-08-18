/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.registry;

import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Registration point for any kind of lifecycle sources per device.
 */
public interface ItemLifeCycleRegistry {

    /**
     * register given life cycle source to known sources of device
     *
     * @param lifeCycleSource life cycle changes provider
     * @return closeable registration
     */
    Registration registerLifeCycleSource(ItemLifeCycleSource lifeCycleSource);

    /**
     * close all existing registrations
     */
    void clear();

    /**
     * @return registered sources
     */
    Iterable<ItemLifeCycleSource> getLifeCycleSources();
}
