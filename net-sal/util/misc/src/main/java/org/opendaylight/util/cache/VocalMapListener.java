/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.cache;

import java.util.Set;

/**
 * A listener of vocal maps.
 *
 * @see VocalAgeOutHashMap
 *
 * @param <V> the value class
 *
 * @author Simon Hunt
 */
public interface VocalMapListener<V> {

    /**
     * Callback invoked when values are removed from the map to which we
     * are listening. The set will always contain one or more elements.
     *
     * @param values the values removed
     */
    void valuesRemoved(Set<V> values);
}
