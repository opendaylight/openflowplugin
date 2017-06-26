/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.clients;

import java.util.Arrays;

import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing waiting on message
 * @author michal.polkorab
 */
public class WaitForMessageEvent implements ClientEvent {

    private static final Logger LOG = LoggerFactory.getLogger(WaitForMessageEvent.class);
    private byte[] headerExpected;
    private byte[] headerReceived;

    /**
     * @param headerExpected header (first 8 bytes) of expected message
     */
    public WaitForMessageEvent(byte[] headerExpected) {
        this.headerExpected = new byte[headerExpected.length];
        System.arraycopy(headerExpected, 0, this.headerExpected, 0, headerExpected.length);
    }

    @Override
    public boolean eventExecuted() {
        if (headerReceived == null) {
            return false;
        }
        if (!Arrays.equals(headerExpected, headerReceived)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("expected msg: {}", ByteBufUtils.bytesToHexString(headerExpected));
                LOG.debug("received msg: {}", ByteBufUtils.bytesToHexString(headerReceived));
            }
            return false;
        }
        LOG.debug("Headers OK");
        return true;
    }

    /**
     * @param headerReceived header (first 8 bytes) of expected message
     */
    public void setHeaderReceived(byte[] headerReceived) {
        if (headerReceived != null) {
            this.headerReceived = new byte[headerReceived.length];
            System.arraycopy(headerReceived, 0, this.headerReceived, 0, headerReceived.length);
        }
    }
}
