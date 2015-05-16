/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.registry.meter;

import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 15.4.2015.
 */
public interface DeviceMeterRegistry extends AutoCloseable {

    void store(MeterId meterId);

    void markToBeremoved(MeterId meterId);

    void removeMarked();

    List<MeterId> getAllMeterIds();

    @Override
    void close();

}
