/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Martin Bobak <mbobak@cisco.com> on 26.2.2015.
 */
public class Xid {

    private AtomicLong value;

    public Xid(Long xid) {
        Preconditions.checkNotNull(xid);
        value = new AtomicLong(xid);
    }

    public long getNextValue() {
        return value.incrementAndGet();
    }
}
