/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.path;

import java.util.Set;

/**
 * Abstraction of a traffic selector, i.e. a match.
 *
 * @author Thomas Vachuska
 */
public interface TrafficSelector {

    /**
     * Returns the set of fields for selecting traffic. In order for the
     * traffic to match the selector, it must match all the fields.
     *
     * @return set of fields to be matched
     */
    Set<Field> fields();

}
