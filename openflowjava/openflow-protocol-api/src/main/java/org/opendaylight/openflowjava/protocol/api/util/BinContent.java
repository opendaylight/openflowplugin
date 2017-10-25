/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.util;

/**
 * @author michal.polkorab
 *
 */
public abstract class BinContent {

    private BinContent() {
        //not called
    }

    /**
     * @param value input integer value (might be negative)
     * @return int part wrapped in long (always positive)
     */
    public static long intToUnsignedLong(int value) {
        return value & 0x00000000ffffffffL;
    }

    /**
     * @param value input long value
     * @return long cut into int
     */
    public static int longToSignedInt(long value) {
        return (int) value;
    }
}
