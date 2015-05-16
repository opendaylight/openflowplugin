/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.meter;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 15.4.2015.
 */
public class DeviceMeterRegistryImpl implements DeviceMeterRegistry {

    private final List<MeterId> meterIds = new ArrayList<>();
    private final List<MeterId> marks = new ArrayList<>();

    @Override
    public void store(final MeterId meterId) {
        meterIds.add(meterId);
    }

    @Override
    public void markToBeremoved(final MeterId meterId) {
        marks.add(meterId);
    }

    @Override
    public void removeMarked() {
        synchronized (meterIds) {
            meterIds.removeAll(marks);
        }
        synchronized (marks) {
            marks.clear();
        }
    }

    @Override
    public List<MeterId> getAllMeterIds() {
        return meterIds;
    }

    @Override
    public void close() {
        synchronized (meterIds) {
            meterIds.clear();
        }
        synchronized (marks) {
            marks.clear();
        }
    }
}
