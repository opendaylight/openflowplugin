/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.portstatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.opendaylight.openflowplugin.api.openflow.registry.CommonDeviceRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatus;

public class DevicePortStatusRegistry implements CommonDeviceRegistry<PortStatus> {
    private final List<PortStatus> registry = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void store(final PortStatus portStatus) {
        registry.add(portStatus);
    }

    @Override
    public void addMark(final PortStatus portStatus) {
        // NOOP
    }

    @Override
    public void processMarks() {
        // NOOP
    }

    @Override
    public void forEach(final Consumer<PortStatus> consumer) {
        registry.forEach(consumer);
    }

    @Override
    public int size() {
        return registry.size();
    }

    @Override
    public void close() {
        registry.clear();
    }
}
