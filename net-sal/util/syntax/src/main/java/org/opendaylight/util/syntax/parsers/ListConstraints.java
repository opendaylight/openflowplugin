/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

/** 
 * Interface defining a set of constraints and hints for parsing list
 * parameter values.
 *
 * @author Thomas Vachuska 
 */
public abstract interface ListConstraints extends Constraints {

    /**
     * Default value for the list item separator.
     */
    public static final String SEPARATOR = ",";

    /** Keyword specifying item separator override.  */
    public static final String KW_SEPARATOR = "separator";

    /**
     * Keyword specifying minimum number of items in the list.
     */
    public static final String KW_MIN_LENGTH = "minLength";

    /**
     * Keyword specifying maximum number of items in the list.
     */
    public static final String KW_MAX_LENGTH = "maxLength";

    /**
     * Returns the character used as the item separator.
     * 
     * @return item separator string
     */
    String getSeparator();

    /**
     * Returns the minimum length of the list or null of there is not one.
     * 
     * @return minimum list length
     */
    Integer getMinLength();

    /** 
     * Returns the maximum length of the list or null of there is not one.
     * 
     * @return maximum list length
     */
    Integer getMaxLength();
       
}


