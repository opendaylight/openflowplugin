/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;

/** 
 * Interface for validating an object against a set of constraints.
 *
 * @author Thomas Vachuska 
 */
public abstract interface Constraints {

    /**
     * Determines whether or not the given object is valid in the context of
     * these constraints.
     * 
     * @param object object to be validated against the constraints
     * @return True if the given object matches this set of constraints; false
     *         otherwise.
     */
    boolean isValid(Serializable object);

}



