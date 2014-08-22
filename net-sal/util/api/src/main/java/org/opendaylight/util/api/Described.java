/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Classes that implement this interface are declaring that they
 * have a name and a description.
 * 
 * @author Thomas Vachuska
 * @author Steve Britt
 * @author Simon Hunt
 * @author Scott Simes
 */
public interface Described extends Named {

    /**
     * Returns the description of the entity.
     *
     * @return entity description
     */
    public String description();

}
