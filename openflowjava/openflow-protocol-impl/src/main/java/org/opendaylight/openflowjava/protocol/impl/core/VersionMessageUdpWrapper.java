/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;

/**
 * Wraps received messages (includes version) and sender address

 * @author michal.polkorab
 */
public class VersionMessageUdpWrapper extends VersionMessageWrapper {
    private final InetSocketAddress address;

    /**
     * @param version Openflow wire version
     * @param messageBuffer ByteBuf containing binary message
     * @param address sender address
     */
    public VersionMessageUdpWrapper(final short version, final ByteBuf messageBuffer, final InetSocketAddress address) {
        super(version, messageBuffer);
        this.address = address;
    }

    /**
     * @return sender address
     */
    public InetSocketAddress getAddress() {
        return address;
    }
}