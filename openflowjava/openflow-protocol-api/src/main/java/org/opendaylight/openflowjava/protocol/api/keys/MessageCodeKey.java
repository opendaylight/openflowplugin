/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

/**
 * @author michal.polkorab
 */
public class MessageCodeKey {

    private short msgVersion;
    private int msgType;
    private Class<?> clazz;

    /**
     * Constructor
     * @param version wire protocol version
     * @param value used as distinguisher (read from binary data / buffer)
     * @param clazz class of object that is going to be deserialized
     */
    public MessageCodeKey(short version, int value, Class<?> clazz) {
        this.msgVersion = version;
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
        result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
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
        if (!(obj instanceof MessageCodeKey)) {
            return false;
        }
        MessageCodeKey other = (MessageCodeKey) obj;
        if (clazz == null) {
            if (other.clazz != null) {
                return false;
            }
        } else if (!clazz.equals(other.clazz)) {
            return false;
        }
        if (msgType != other.msgType) {
            return false;
        }
        if (msgVersion != other.msgVersion) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "msgVersion: " + msgVersion + " objectClass: " + clazz.getName() + " msgType: " + msgType;
    }
}