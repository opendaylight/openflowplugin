/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Key for a message code.
 *
 * @author michal.polkorab
 */
public class MessageCodeKey {
    private final Uint8 msgVersion;
    private final int msgType;
    private final Class<?> clazz;

    /**
     * Constructor.
     *
     * @param version wire protocol version
     * @param value used as distinguisher (read from binary data / buffer)
     * @param clazz class of object that is going to be deserialized
     */
    public MessageCodeKey(final Uint8 version, final int value, final Class<?> clazz) {
        this.msgVersion = requireNonNull(version);
        this.msgType = value;
        this.clazz = clazz;
    }

    public int getMsgType() {
        return this.msgType;
    }

    public Class<?> getClazz() {
        return this.clazz;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (clazz == null ? 0 : clazz.hashCode());
        result = prime * result + msgType;
        result = prime * result + msgVersion.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MessageCodeKey)) {
            return false;
        }
        MessageCodeKey other = (MessageCodeKey) obj;
        return Objects.equals(clazz, other.clazz) && msgType == other.msgType && msgVersion.equals(other.msgVersion);
    }

    @Override
    public String toString() {
        return "msgVersion: " + msgVersion + " objectClass: " + clazz.getName() + " msgType: " + msgType;
    }
}
