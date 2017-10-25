/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization;


/**
 * Class used as a key in {@link DeserializerRegistryImpl}
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class MessageTypeCodeKey {

    private final short msgType;
    private final short msgVersion;

    /**
     * @param msgVersion protocol version
     * @param msgType type code of message
     */
    public MessageTypeCodeKey(short msgVersion, short msgType) {
        this.msgType = msgType;
        this.msgVersion = msgVersion;
    }

    /**
     * @return the msgType
     */
    public short getMsgType() {
        return msgType;
    }

    /**
     * @return the msgVersion
     */
    public short getMsgVersion() {
        return msgVersion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + msgType;
        result = prime * result + msgVersion;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MessageTypeCodeKey other = (MessageTypeCodeKey) obj;
        if (msgType != other.msgType) {
            return false;
        }
        if (msgVersion != other.msgVersion) {
            return false;
        }
        return true;
    }
}