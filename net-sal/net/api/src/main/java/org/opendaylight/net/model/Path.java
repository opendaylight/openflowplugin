/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import java.util.List;

/**
 * Path is an ordered collection of {@link Link links}, where adjoining links'
 * {@link ConnectionPoint connection points} share the same {@link Device device}.
 * The outer connection points of its outer links are the path source and
 * destination. If the path contains eny {@link HostLink node links}, they must
 * be at the first or the last elements in the list.
 * <p>
 * Path is essentially a compound {@link Link}.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface Path extends Link {

    /**
     * Directed set of links representing the network path.
     *
     * @return list of links
     */
    List<Link> links();

}
