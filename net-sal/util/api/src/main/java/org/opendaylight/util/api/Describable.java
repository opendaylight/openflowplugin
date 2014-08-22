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
 * have a name and a description which can each be modified.
 *
 * @author Steve Britt
 * @author Thomas Vachuska
 * @author Scott Simes
 * @author Simon Hunt
 */
public interface Describable extends Described, Nameable {

    /**
     * Set the entity description.
     *
     * @param description new entity description
     */
    public void setDescription(String description);

}
