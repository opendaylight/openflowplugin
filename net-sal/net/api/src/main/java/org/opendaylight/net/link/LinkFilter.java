/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link;

import org.opendaylight.net.model.Link;

/**
 * Interface for filtering {@link Link}s
 * 
 * @author Ryan Tidwell
 */
public interface LinkFilter {

    /**
     * Test whether the given {@link Link} matches this filter
     * 
     * @return true if given {@link Link} matches, false otherwise
     */
    boolean matches(Link link);
}
