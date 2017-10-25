/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.registry;

import java.util.function.Consumer;

public interface CommonDeviceRegistry<KEY> extends AutoCloseable {

    /**
     * Store KEY in device registry.
     * @param key device registry key
     */
    void store(KEY key);

    /**
     * Add mark for specified KEY.
     * @param key device registry key
     */
    void addMark(KEY key);

    /**
     * Process marked keys.
     */
    void processMarks();

    /**
     * Iterate over all keys in device registry.
     * @param consumer key consumer
     */
    void forEach(Consumer<KEY> consumer);

    /**
     * Get device registry size.
     * @return device registry size
     */
    int size();

    @Override
    void close();

}
