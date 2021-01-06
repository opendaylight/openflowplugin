/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Enhanced MessageCodeKey.
 *
 * @author michal.polkorab
 */
public class EnhancedMessageCodeKey<T extends DataContainer> extends MessageCodeKey<T> {
    private final int msgType2;

    /**
     * Constructor.
     *
     * @param version wire protocol version
     * @param value used as distinguisher
     * @param value2 used as detailed distinguisher
     * @param clazz class of object that is going to be deserialized
     */
    public EnhancedMessageCodeKey(final short version, final int value, final int value2, final Class<T> clazz) {
        super(version, value, clazz);
        this.msgType2 = value2;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + msgType2;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        return super.equals(obj) && getClass() == obj.getClass()
            && msgType2 == ((EnhancedMessageCodeKey<?>) obj).msgType2;
    }

    @Override
    public String toString() {
        return super.toString() + " msgType2: " + msgType2;
    }
}
