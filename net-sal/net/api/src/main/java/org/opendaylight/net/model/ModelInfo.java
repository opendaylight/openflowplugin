/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import java.util.Set;

/**
 * Abstraction of an information carrier.
 *
 * @author Thomas Vachuska
 * @author Shaun Wackerly
 */
public interface ModelInfo<F extends Enum<?>> {

    /**
     * Returns the set of dirty fields.
     *
     * @return set of dirty fields
     */
    Set<F> dirtyFields();

}
