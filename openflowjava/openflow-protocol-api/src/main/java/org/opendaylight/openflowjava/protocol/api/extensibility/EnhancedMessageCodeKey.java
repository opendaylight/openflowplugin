/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Enhanced MessageCodeKey.
 *
 * @author michal.polkorab
 */
public class EnhancedMessageCodeKey extends MessageCodeKey {
    private final int msgType2;

    /**
     * Constructor.
     *
     * @param version wire protocol version
     * @param value used as distinguisher
     * @param value2 used as detailed distinguisher
     * @param clazz class of object that is going to be deserialized
     */
    public EnhancedMessageCodeKey(final Uint8 version, final int value, final int value2, final Class<?> clazz) {
        super(version, value, clazz);
        this.msgType2 = value2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + msgType2;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EnhancedMessageCodeKey other = (EnhancedMessageCodeKey) obj;
        if (msgType2 != other.msgType2) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " msgType2: " + msgType2;
    }
}
