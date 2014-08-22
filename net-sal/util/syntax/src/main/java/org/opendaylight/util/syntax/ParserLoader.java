/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import org.opendaylight.util.syntax.parsers.TypedParser;

/**
 * Interface defining mechanism to load and register parameter and constraint
 * parsers.
 *
 * @author Thomas Vachuska 
 */
public abstract interface ParserLoader {

    /**
     * Returns the parser with the specified name and from the specified
     * interface pool.
     * 
     * @param name symbolic name of the parser
     * @param parserInterface interface fulfilled by the parser
     * @return parser instance with the given name; null of none found
     */
    public TypedParser getParser(String name, Class<?> parserInterface);

    /**
     * Registers the specified parser in the pool of parsers that implement
     * the specified interface.
     * 
     * @param parser instance to be registred
     * @param parserInterface interface fulfilled by the parser
     */
    public void addParser(TypedParser parser, Class<?> parserInterface);

}
