/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

/**
 * Abstraction of a network link connecting infrastructure devices and/or nodes.
 * It is an ordered association of a source and a destination
 * {@link org.opendaylight.net.model.ConnectionPoint connection points} and thus
 * inherently represents uni-directional network connectivity.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface Link extends Model {

    /**
     * Represents the type of link.
     */
    public enum Type {
        /** Represents direct links. */
        DIRECT_LINK,

        /** Represents indirect/multi-hop links. */
        MULTIHOP_LINK,

        /** Represents tunnel links. */
        TUNNEL,

        /** Represents node/edge links. */
        NODE
    }

    /**
     * Returns the type of the link.
     *
     * @return link type
     */
    Type type();

    /**
     * Returns the link source connection point.
     *
     * @return source connection point
     */
    ConnectionPoint src();

    /**
     * Returns the link destination connection point.
     *
     * @return dest connection point
     */
    ConnectionPoint dst();

}
