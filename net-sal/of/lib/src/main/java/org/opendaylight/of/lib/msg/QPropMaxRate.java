/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

/**
 * Represents a MAX_RATE property of a {@link Queue}.
 *
 * @author Simon Hunt
 */
public class QPropMaxRate extends QPropRate {
    /**
     * Constructor invoked by QueueFactory.
     *
     * @param header the property header
     */
    QPropMaxRate(Header header) {
        super(header);
    }
}
