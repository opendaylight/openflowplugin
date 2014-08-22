/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

import java.io.Serializable;

/**
 * Provides the ability for the transport objects to carry the data between
 * application layers and tiers
 * 
 * @param <T> type of the transfer object (DTO)
 * @param <I> type of the transportable's id. It should be an immutable type.
 *        It is critical this type implements equals() and hashCode()
 *        correctly.
 */
public interface Transportable<T, I extends Serializable> extends Serializable {

    /**
     * Gets the id of this object.
     * <P>
     * A type for the identified must be specified to retrieve the id because
     * identified objects might be inheritable. For example, assume
     * {@code Employee} extends from {@code Person} and {@code Person}
     * implements {@code Transportable<Person, Long>}. The following code
     * would be possible.
     * 
     * <pre>
     * 
     * Id&lt;Person, Long&gt; id = employee.getId();
     * Id&lt;Employee, Long&gt; id = employee.getId();
     * 
     * <pre>
     * 
     * @return the id of this object
     */
    public <E extends T> Id<E, I> getId();
}
