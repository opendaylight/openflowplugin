/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.pkt;

/**
 * A piece of information about a <em>Packet-In</em> message that may help
 * in deciding what to do with it.
 * <p>
 * Each hint has a type; the standard types are defined by {@link HintType}.
 * For each standard type there is a corresponding concrete class that holds
 * the payload of the hint.
 * <p>
 * To allow experimenters to define their own hint types, each type is
 * encoded as an integer. Note that positive integers are reserved;
 * experimenters should express their custom hint types using negative
 * integers.
 *
 * @author Simon Hunt
 */
public interface Hint {
    /** Returns the hint type encoded as an integer.
     *
     * @return the encoded type
     */
    int getEncodedType();

    /** Returns the hint type, if it is a standard type. Non-standard types
     * will return null, and will need to be differentiated via
     * {@link #getEncodedType()}.
     *
     * @return the hint type
     */
    HintType getType();

}
