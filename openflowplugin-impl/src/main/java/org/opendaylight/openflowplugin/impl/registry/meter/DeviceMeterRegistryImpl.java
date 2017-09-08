/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.meter;

import com.google.common.annotations.VisibleForTesting;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;

@ThreadSafe
public class DeviceMeterRegistryImpl implements DeviceMeterRegistry {

    private final Queue<MeterId> meterIds = new ConcurrentLinkedQueue<>();
    private final Queue<MeterId> marks = new ConcurrentLinkedQueue<>();

    @Override
    public void store(final MeterId meterId) {
        marks.remove(meterId);
        meterIds.add(meterId);
    }

    @Override
    public void addMark(final MeterId meterId) {
        marks.add(meterId);
    }

    @Override
    public void processMarks() {
        meterIds.removeAll(marks);
        marks.clear();
    }

    @Override
    public void forEach(final Consumer<MeterId> consumer) {
        meterIds.forEach(consumer);
    }

    @Override
    public int size() {
        return meterIds.size();
    }

    @Override
    public void close() {
        meterIds.clear();
        marks.clear();
    }

    @VisibleForTesting
    Queue<MeterId> getAllMeterIds() {
        return meterIds;
    }
}
