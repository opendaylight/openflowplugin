/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
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
 * Class used as a key in {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry}.
 *
 * @author michal.polkorab
 * @author timotej.kubas
 * @param <E> message type (class)
 */
public class MessageTypeKey<E> {

    private final Class<? extends E> msgType;
    private final Uint8 msgVersion;

    /**
     * Constructor.
     *
     * @param msgVersion protocol version
     * @param msgType type of message - class of serialized object
     */
    public MessageTypeKey(final Uint8 msgVersion, final Class<? extends E> msgType) {
        this.msgType = msgType;
        this.msgVersion = requireNonNull(msgVersion);
    }

    @Override
    public String toString() {
        return "msgVersion: " + msgVersion + " objectType: " + msgType.getName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (msgType == null ? 0 : msgType.hashCode());
        result = prime * result + msgVersion.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MessageTypeKey)) {
            return false;
        }
        MessageTypeKey<?> other = (MessageTypeKey<?>) obj;
        if (!Objects.equals(msgType, other.msgType)) {
            return false;
        }
        if (!msgVersion.equals(other.msgVersion)) {
            return false;
        }
        return true;
    }
}
