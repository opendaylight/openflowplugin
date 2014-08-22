/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.path;

/**
 * Abstraction of a traffic treatment policy.
 *
 * @author Thomas Vachuska
 */
public interface TrafficTreatment {

    // TODO: work in progress

    /**
     * Returns the priority to be assigned to flow rules.
     *
     * @return flow rule priority
     */
    int priority();

    /**
     * Returns the cookie to be assigned to flow rules.
     *
     * @return flow rule cookie
     */
    long cookie();

}
