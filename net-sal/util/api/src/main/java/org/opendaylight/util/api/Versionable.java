/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Provides the ability to carry the JPA Version value with the object
 * 
 * @author Scott Simes
 * @author Fabiel Zuniga
 * @author Stephen Cobbe
 */
public interface Versionable {

    /**
     * The JPA persistence version identification. Used for the optimistic
     * locking aspect of the persistence layer. Allows for conversion from
     * Transport object to Data Access object to properly interact with the
     * persistence layer
     * 
     * @return the internal version number
     */
    public Long getVersion();

    /**
     * Sets the version.
     * 
     * @param version the version.
     */
    public void setVersion(Long version);
}
