/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

/**
 * Implemented by enums that provide logical names to a set of coded values.
 *
 * @author Simon Hunt
 */
public interface CodeEnum {

    /** Returns the code for the constant.
     *
     * @return the code
     */
    int getCode();
}
