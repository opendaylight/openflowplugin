/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.path;

import org.opendaylight.net.model.Path;

/**
 * Service for paving paths between network end-station nodes.
 *
 * @author Thomas Vachuska
 */
public interface PathPavingService {

    /**
     * Paves the specified path, for the traffic matching the supplied selector.
     *
     * @param path      path to be provisioned
     * @param selector  selector identifying traffic to which the steering
     *                  along the given path should be applied
     * @param treatment policy for treatment of traffic
     */
    void pavePath(Path path, TrafficSelector selector, TrafficTreatment treatment);

}
