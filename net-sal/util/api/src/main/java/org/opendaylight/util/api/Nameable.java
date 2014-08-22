/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Classes that implement this interface are declaring that they have a name
 * which can be modified.
 * 
 * @author Steve Britt
 * @author Thomas Vachuska
 * @author Scott Simes
 * @author Simon Hunt
 * @author Fabiel Zuniga
 */
public interface Nameable extends Named {

    /**
     * Set the name of the entity.
     * 
     * @param name new entity name
     */
    public void setName(String name);
}
