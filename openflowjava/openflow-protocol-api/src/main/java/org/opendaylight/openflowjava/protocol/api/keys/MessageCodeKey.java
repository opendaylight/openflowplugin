/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import java.util.Objects;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Key for a message code.
 *
 * @author michal.polkorab
 */
public class MessageCodeKey<T extends DataContainer> {
    private final short msgVersion;
    private final int msgType;
    private final Class<T> clazz;

    /**
     * Constructor.
     *
     * @param version wire protocol version
     * @param value used as distinguisher (read from binary data / buffer)
     * @param clazz class of object that is going to be deserialized
     */
    public MessageCodeKey(final short version, final int value, final Class<T> clazz) {
        this.msgVersion = version;
        this.msgType = value;
        this.clazz = clazz;
    }

    public final int getMsgType() {
        return this.msgType;
    }

    public final Class<?> getClazz() {
        return this.clazz;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(clazz);
        result = prime * result + msgType;
        result = prime * result + msgVersion;
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
        final MessageCodeKey<?> other = (MessageCodeKey<?>) obj;
        return msgType == other.msgType && msgVersion == other.msgVersion && Objects.equals(clazz, other.clazz);
    }

    @Override
    public String toString() {
        return "msgVersion: " + msgVersion + " objectClass: " + clazz.getName() + " msgType: " + msgType;
    }
}
