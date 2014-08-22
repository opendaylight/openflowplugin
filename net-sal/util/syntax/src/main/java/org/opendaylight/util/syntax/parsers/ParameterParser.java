/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;
import org.opendaylight.util.syntax.Parameters;

/** 
 * Interface for objects that can validate and parse a token into a
 * specific object.
 *
 * @author Thomas Vachuska
 */
public abstract interface ParameterParser extends TypedParser {
    
    /** 
     * Returns a non-null object if the parsing of the given token was
     * succesful and if the object value complied with the specified
     * constraints, if any were specified.  Returns null
     * otherwise.  
     *
     * @param token String to be decoded/parsed into a Serializable entity.
     * @param constraints Constraints &amp; hints to use for parsing
     * and for validating the resulting Serializable.
     * @param soFar Parameters accumulated by the parsing process thus far.
     * @return A serializable entity created based on the token value.
     */
    Serializable parse(String token, Constraints constraints, Parameters soFar);
    
}
