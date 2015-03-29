/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 26.2.2015.
 */
public class Xid {

    private long value;

    public Xid(final long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
