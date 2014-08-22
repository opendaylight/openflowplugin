/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.match;

/**
 * Denotes the matching mode for VLAN ID matching.  Package private as this
 * is an implementation detail, not exposed through the public API.
 *
 * @author Simon Hunt
 */
enum OxmVlanId {
    /** Match when <em>no</em> VLAN ID is present; Since 1.3. */
    NONE(0x0000),
    /** Match when <em>any</em> VLAN ID is present; Since 1.3. */
    PRESENT(0x1000),
    /** Match when the <em>specified</em> VLAN ID is present; Since 1.3. */
    EXACT(0xabcd),
    ;

    private int value;

    OxmVlanId(int value) {
        this.value = value;
    }

    /** Returns the internal value.
     *
     * @return the value
     */
    int getValue() {
        return value;
    }
}
