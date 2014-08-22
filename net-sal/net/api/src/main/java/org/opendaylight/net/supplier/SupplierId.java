/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.supplier;

import java.io.Serializable;

/**
 * Supplier identifier. This ID is expected to follow Java package-like
 * convention to provide a hierarchical name-space.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public class SupplierId implements Serializable {

    private static final long serialVersionUID = 1177222047613279145L;

    private final String id;

    /**
     * Creates a supplier ID using the given string. The string is expected to
     * follow Java package-like convention to provide a hierarchical name-space.
     *
     * @param id string form of the supplier ID
     */
    public SupplierId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplierId that = (SupplierId) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
