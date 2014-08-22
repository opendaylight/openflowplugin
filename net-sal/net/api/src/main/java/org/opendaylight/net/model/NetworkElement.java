/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

/**
 * Abstraction of a network elements such as a {@link Device} or a {@link Host}.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface NetworkElement extends Model {
    
    /**
     * Characterization of the network element function.
     */
    public enum Type {
        SWITCH, ROUTER, ACCESS_POINT, NIC, FIREWALL, IDS, OTHER
    }

    /**
     * Returns the globally unique identifier of the network element. It is an
     * opaque entity.
     * 
     * @return unique identifier
     */
    ElementId id();
    
    /**
     * Returns the friendly name of the network element.
     *  
     * @return name
     */
    String name();

    /**
     * Returns the network element type, e.g. switch, router, access point,
     * NIC.
     * 
     * @return network element type
     */
    Type type();

}
