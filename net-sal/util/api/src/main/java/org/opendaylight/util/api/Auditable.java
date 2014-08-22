/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;


import java.util.Date;

/**
 * Classes that implement this interface are declaring that they
 * provide audit information to track who created it,
 * last modified it, and when, with mutable modification data.
 *
 * @author Steve Britt
 * @author Simon Hunt
 * @author Thomas Vachuska
 * @author Scott Simes
 */
public interface Auditable extends Audited {

    /**
     * Sets the identity of the entity or user who
     * last modified this entity.
     *
     * @param modifierId the identity of the modifier
     */
    public void setModifiedBy(String modifierId);
    
    /**
     * Sets the time at which this entity was last modified.
     *
     * @param timestamp the last modification timestamp
     */
    public void setModifiedAt(Date timestamp);

}
