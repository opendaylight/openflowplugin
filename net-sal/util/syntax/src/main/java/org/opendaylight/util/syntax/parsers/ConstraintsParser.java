/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.util.Properties;
import org.opendaylight.util.format.TokenTranslator;
import org.opendaylight.util.syntax.ParserLoader;

/** 
 * Interface for generating constraints out of the syntax node
 * definition database.
 *
 * @author Thomas Vachuska 
*/
public abstract interface ConstraintsParser extends TypedParser {
    
    /** 
     * Returns an object describing particular constraints as defined
     * in the specified property database.
     *
     * @param db Set of properties that can be used to parametrize the
     * constraints implementation.
     * @param translator A token translator that can be used for
     * locale-specific or other types of value translation.
     * @param parserLoader Reference to the parameter and constraints parser loader.
     * @return A concrete implementation of the {@link Constraints} interface.
     */
    Constraints parse(Properties db, TokenTranslator translator,
                      ParserLoader parserLoader);
    
}
