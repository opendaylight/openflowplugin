/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Classes that implement this interface are declaring that they have a name.
 * 
 * @author Steve Britt
 * @author Thomas Vachuska
 * @author Scott Simes
 * @author Simon Hunt
 * @author Fabiel Zuniga
 */
public interface Named {

    /**
     * Returns the name of the entity.
     * 
     * @return name of the entity
     */
    public String name();
}
