/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt;

/**
 * An abstract base implementation of {@link Hint} that handles the hint type.
 * Subclasses must provide the payload implementation.
 *
 * @author Simon Hunt
 */
public abstract class AbstractHint implements Hint {
    private final int encodedType;

    /** Constructs the hint.
     *
     * @param encodedType the hint type encoded as an integer
     */
    protected AbstractHint(int encodedType) {
        this.encodedType = encodedType;
    }

    @Override
    public int getEncodedType() {
        return encodedType;
    }

    @Override
    public HintType getType() {
        return HintType.decode(encodedType);
    }

    @Override
    public String toString() {
        return "{Hint:" + HintType.typeToString(encodedType) + "}";
    }

}
