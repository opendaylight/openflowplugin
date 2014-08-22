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
 * have an identifier comprising some unique id string which
 * can be set.
 *
 * @author Thomas Vachuska
 * @author Scott Simes
 * @author Simon Hunt
 * @author Steve Britt
 */
public interface Identifiable extends Identified {

    /**
     * Set the unique id for this entity.
     *
     * @param uid the unique id
     */
    public void setUid(String uid);

}
