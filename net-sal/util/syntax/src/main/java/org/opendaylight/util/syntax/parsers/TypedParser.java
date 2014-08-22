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
 * Base interface for all parsers associated with a type token.
 *
 * @author Thomas Vachuska 
 */
public abstract interface TypedParser extends Serializable {
    
    /**
     * Returns the string which serves as a value type token for parameters of
     * this type.
     * 
     * @return type token string of the parser
     */
    public String getTypeToken();
    
}
