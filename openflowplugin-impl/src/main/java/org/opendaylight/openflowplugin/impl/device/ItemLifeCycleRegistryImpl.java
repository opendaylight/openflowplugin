/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.openflowplugin.api.openflow.registry.ItemLifeCycleRegistry;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * default implementation
 */
public class ItemLifeCycleRegistryImpl implements ItemLifeCycleRegistry {

    private final Set<ItemLifeCycleSource> registry;

    public ItemLifeCycleRegistryImpl() {
        registry = Collections.newSetFromMap(new ConcurrentHashMap<ItemLifeCycleSource, Boolean>());
    }


    @Override

    public Registration registerLifeCycleSource(final ItemLifeCycleSource lifeCycleSource) {
        registry.add(lifeCycleSource);
        return new Registration() {
            @Override
            public void close() throws Exception {
                registry.remove(lifeCycleSource);
            }
        };
    }

    @Override
    public void clear() {
        registry.clear();
    }

    @Override
    public Iterable<ItemLifeCycleSource> getLifeCycleSources() {
        return Collections.unmodifiableCollection(registry);
    }
}
