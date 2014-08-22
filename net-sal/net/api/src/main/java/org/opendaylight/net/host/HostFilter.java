/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host;

import org.opendaylight.net.model.Host;

/**
 * A filter which matches against information in a {@link org.opendaylight.net.model.Host}.
 *
 * @author Shaun Wackerly
 * @author Vikram Bobade
 */
public interface HostFilter {
    
    /**
     * Returns whether the given node matches this filter or not.
     * 
     * @param host the given node
     * @return true if the node matches, false if not
     */
    boolean matches(Host host);

}
