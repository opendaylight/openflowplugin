/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * This interface facilitates the obtaining of name and description for a given enumeration constant.
 *
 * @author Simon Hunt
 */
public interface LocalizedEnums {

    /** Returns the localized name of the specified constant.
     *
     * @param enumConstant the constant
     * @return the name
     */
    public String getName(Enum<?> enumConstant);

    /** Returns the localized description of the specified constant.
     *
     * @param enumConstant the constant
     * @return the description
     */
    public String getDescription(Enum<?> enumConstant);

}
