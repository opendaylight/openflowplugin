/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;
import java.io.File;
import java.util.Locale;
import java.util.Properties;

import org.opendaylight.util.format.TokenTranslator;
import org.opendaylight.util.syntax.Parameters;
import org.opendaylight.util.syntax.ParserLoader;

/** 
 * Builtin parser for file values.  
 *
 * @author Thomas Vachuska
 */
public class FileParameterParser extends AbstractParameterParser {

    private static final long serialVersionUID = -4510624861425950307L;

    /** 
     * Default constructor.
     */
    public FileParameterParser() {
    }

    /**
     * Constructs a new file parameter parser using the given locale.
     *
     * @param locale Locale to associate with this parser.
     */
    public FileParameterParser(Locale locale) {
        super(locale);
    }

    /**
     * @see TypedParser#getTypeToken
     */
    @Override
    public String getTypeToken () {
        return "file";
    }
    
    /**
     * Creates an instance of {@link FileConstraints}.
     *
     * @see ConstraintsParser#parse
     *
     * @return {@link FileConstraints} instance created using the
     * specified properties.
     */
    @Override
    public Constraints parse(Properties db, TokenTranslator tr
                             , ParserLoader parserLoader) { 
        String flags = tr.translate(db.getProperty("flags"));
        //  Create the constraint only when we need to.
        if (flags != null)
            return new FileConstraints(flags);
        return null;
    }
    
    /** 
     * Returns a non-null object if the parsing of the given token was
     * succesful and if the object value complied with the specified
     * constraints, if any were specified.
     *
     * @see ParameterParser#parse
     *
     * @return A validated {@link File} instance or null if the
     * resulting File value does not meet the constraints, if any.
     */
    @Override
    public Serializable parse(String token, Constraints constraints,
                              Parameters soFar) {
        File file = new File(token);
        if (constraints != null) {
            if (constraints.isValid(file))
                return file;
        } else if (file.canRead()) {
            return file;
        }
        return null;
    }
    
}
