/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

/**
 * Connection Point is a loose association of a {@link NetworkElement
 * network element} and an {@link Interface} pair using their respective
 * identifiers.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface ConnectionPoint {

    /**
     * Returns the unique identifier of the connection point network element.
     *
     * @return network element identifier
     */
    ElementId elementId();

    /**
     * Returns the connection point interface identifier.
     * 
     * @return interface identifier
     */
    InterfaceId interfaceId();
}
