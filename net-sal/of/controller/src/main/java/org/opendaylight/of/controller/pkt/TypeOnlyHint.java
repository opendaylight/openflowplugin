/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt;

/**
 * Represents a hint that has no contextual data other than its type.
 *
 * @author Simon Hunt
 */
public class TypeOnlyHint extends AbstractHint {
    /**
     * Constructs the hint.
     *
     * @param type the hint type
     */
    protected TypeOnlyHint(HintType type) {
        super(type.encodedType());
    }
}
