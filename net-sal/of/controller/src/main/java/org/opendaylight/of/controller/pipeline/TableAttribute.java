/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline;

import java.util.Set;

/**
 * Composite model for a table attribute.
 *
 * @author Pramod Shanbhag
 */
public interface TableAttribute {
    
    /** Returns the attribute name.
     *    
     * @return the attribute name
     */
    String name();
    
    /** Returns the set of child attributes.
     *    
     * @return the set of child attributes
     */
    Set<TableAttribute> children();
    
    /** Returns true if one or more child attributes exist.
     *    
     * @return true if child attributes exist
     */
    boolean hasChildren();
}
