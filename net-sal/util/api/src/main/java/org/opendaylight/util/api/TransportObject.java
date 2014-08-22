/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

import java.io.Serializable;


/**
 * {@link TransportObject} is a contract between the business logic and the DAO.
 *  This provides the ability for the transport object to carry data between
 *  application layers and tiers. The transport objects could have well 
 *  defined fields as in relational database world or the object could just 
 *  have an abstract Hash map or both. The hash map for transport objects is 
 *  introduced to leverage the schema-less design of nosql databases. 
 *  The implementation of this interface supports a hybrid object that contains
 *  a few well defined fields. If there are columns that need to be 
 *  added/deleted in a dynamic fashion, the extension in the form of hash map
 *  should be used.  
 *    
 * @param <T> Type of the Transport Object
 * @param <I> This is the type of the transportable's row key (primary key). 
 *            It should be an immutable type and must implement equals() 
 *            and hashCode() correctly. * 
 */
public interface TransportObject<T, I extends Serializable> extends Serializable {
    /**
     * The Identification for this object.
     * For example, assume {@code Employee} extends from {@code Person} and 
     * {@code Person} implements {@code TransportObject<Person, Long>}.
     * The following code would be possible. 
     * <pre>
     * Id&lt;Person, Long&gt; id = employee.getId();
     * Id&lt;Employee, Long&gt; id = employee.getId();
     * <pre>

     * @return The object identification. 
     *         This is the row key as mentioned above.
     */
    public <E extends T> Id<E, I> getId();    
}
