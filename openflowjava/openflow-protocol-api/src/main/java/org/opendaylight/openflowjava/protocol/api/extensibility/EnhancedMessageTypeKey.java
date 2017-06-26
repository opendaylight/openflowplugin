/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;


/**
 * More specific key for {@link SerializerRegistry}
 * @author michal.polkorab
 * @param <E> main type
 * @param <F> specific type
 */
public class EnhancedMessageTypeKey<E, F> extends MessageTypeKey<E> {

    private final Class<F> msgType2;

    /**
     * @param msgVersion protocol version
     * @param msgType main type
     * @param msgType2 subtype
     */
    public EnhancedMessageTypeKey(short msgVersion, Class<E> msgType, Class<F> msgType2) {
        super(msgVersion, msgType);
        this.msgType2 = msgType2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((msgType2 == null) ? 0 : msgType2.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        EnhancedMessageTypeKey other = (EnhancedMessageTypeKey) obj;
        if (msgType2 == null) {
            if (other.msgType2 != null) {
                return false;
            }
        } else if (!msgType2.getName().equals(other.msgType2.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " msgType2: " + msgType2.getName();
    }
}
