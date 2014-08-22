/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.net.model.Link.Type;

/**
 * Set of dynamic link attributes which may change over time.
 *
 * @author Marjorie Krueger
 */
public interface LinkInfo {

    /**
     * Returns the link {@link org.opendaylight.net.model.Link.Type}
     *
     * @return link type
     */
    Type type();

}
