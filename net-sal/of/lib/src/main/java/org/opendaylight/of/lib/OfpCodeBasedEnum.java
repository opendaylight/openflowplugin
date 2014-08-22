/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib;

/**
 * Implemented by enums that represent OpenFlow protocol coded values.
 *
 * @author Simon Hunt
 */
public interface OfpCodeBasedEnum extends OfpEnum {
    /** Returns the code for the constant, under the given protocol
     * version.
     *
     * @param pv the protocol version
     * @return the code for the constant
     */
    int getCode(ProtocolVersion pv);
}
