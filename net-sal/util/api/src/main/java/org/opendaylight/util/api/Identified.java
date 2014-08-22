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
 * have an identifier comprising some unique id string.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 * @author Scott Simes
 * @author Steve Britt
 */
public interface Identified {

    /**
     * Returns the unique identifier of the entity.
     *
     * @return unique id of the entity
     */
    public String uid();

}
