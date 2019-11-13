/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;

/**
 * Wraps received messages (includes version).
 *
 * @author michal.polkorab
 */
public class VersionMessageWrapper {
    private final short version;
    private final ByteBuf messageBuffer;

    /**
     * Constructor.
     *
     * @param version version decoded in {@link OFVersionDetector}
     * @param messageBuffer message received from {@link OFFrameDecoder}
     */
    public VersionMessageWrapper(final short version, final ByteBuf messageBuffer) {
        this.version = version;
        this.messageBuffer = requireNonNull(messageBuffer);
    }

    /**
     * Returns the version version decoded in {@link OFVersionDetector}.
     *
     * @return the version version decoded in {@link OFVersionDetector}
     */
    public short getVersion() {
        return version;
    }

    /**
     * Returns the messageBuffer message received from {@link OFFrameDecoder}.
     *
     * @return the messageBuffer message received from {@link OFFrameDecoder}
     */
    public ByteBuf getMessageBuffer() {
        return messageBuffer;
    }
}
