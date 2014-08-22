/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.util.Locale;

/** 
 * Builtin abstraction of a locale-specific parser. Delegates implementation
 * responsibilities to derived classes.
 *
 * @author Thomas Vachuska
 *
 */
public abstract class AbstractParameterParser 
                        implements ParameterParser, ConstraintsParser {

    private static final long serialVersionUID = -4841774922712289116L;

    /** Locale with which this parser is associated. */
    protected Locale locale;

    /** 
     * Default constructor.
     */
    protected AbstractParameterParser() {
    }

    /**
     * Constructs a new parser using the given locale.
     *
     * @param locale Locale to associate with this parser.
     */
    protected AbstractParameterParser(Locale locale) { 
        this.locale = locale; 
    }

    /** 
     * Returns the locale with which this parser is associated.
     *
     * @return Locale associated with this parser.
     */
    public Locale getLocale() { 
        return locale; 
    }

}
