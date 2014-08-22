/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology;

import org.opendaylight.net.model.Link;

/**
 * Abstraction of a link edge weight used when computing path costs.
 *
 * @author Thomas Vachuska
 */
public interface LinkWeight {

    /**
     * Return the weight of the specified link.
     *
     * @param link link edge to be weighted
     * @return unit-less link weight
     */
    double weight(Link link);

}
