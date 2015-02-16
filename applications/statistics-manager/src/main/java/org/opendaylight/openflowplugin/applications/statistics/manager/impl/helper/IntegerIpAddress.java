/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper;

/**
 * 4B base + 4B mask wrapper
 */
public class IntegerIpAddress {

    int ip;
    int mask;

    public IntegerIpAddress(final int ip, final int mask) {
        this.ip = ip;
        this.mask = mask;
    }

    public int getIp() {
        return ip;
    }

    public int getMask() {
        return mask;
    }
}
