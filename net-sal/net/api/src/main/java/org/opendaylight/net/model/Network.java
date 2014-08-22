/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import java.util.Set;

/**
 * Network is a set of connection points.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface Network extends Model {
    
    /**
     * Gets the set of connection points for this network.
     * 
     * @return all connection points in this network
     */
    Set<ConnectionPoint> connectionPoints();
}
