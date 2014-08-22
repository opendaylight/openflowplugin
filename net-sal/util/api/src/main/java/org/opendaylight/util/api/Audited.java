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
 * have audit information available, tracking who created it,
 * modified it, and when.
 *
 * @author Steve Britt
 * @author Simon Hunt
 * @author Thomas Vachuska
 * @author Scott Simes
 */
public interface Audited {

    /**
     * Returns the identity of the entity or user who
     * created this entity.
     *
     * @return identity of the creator
     */
    public String createdBy();

    /**
     * Returns the time at which this entity was created.
     *
     * @return creation time-stamp
     */
    public Date createdAt();

    /**
     * Returns the identity of the entity or user who
     * last modified this entity.
     *
     * @return identity of the modifier
     */
    public String modifiedBy();

    /**
     * Returns the time at which this entity was last modified.
     *
     * @return last modification time-stamp
     */
    public Date modifiedAt();

}
