/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;
import java.util.Locale;
import java.util.Properties;

import org.opendaylight.util.format.TokenTranslator;
import org.opendaylight.util.syntax.Parameters;
import org.opendaylight.util.syntax.ParserLoader;


/**
 * Builtin parser for name=value pair arguments.
 *
 * @author Thomas Vachuska
 */
public class NameValueParameterParser extends AbstractParameterParser {

    private static final long serialVersionUID = -6444506924440069644L;

    /** 
     * Default constructor.
     */
    public NameValueParameterParser() {
    }

    /**
     * Constructs a new string parameter parser using the given locale.
     *
     * @param locale Locale to associate with this parser.
     */
    public NameValueParameterParser(Locale locale) {
        super(locale);
    }

    /**
     * @see TypedParser#getTypeToken
     */
    @Override
    public String getTypeToken() {
        return "pair";
    }
    
    /**
     * Creates an instance of {@link NameValueConstraints}.
     *
     * @see ConstraintsParser#parse
     *
     * @return {@link NameValueConstraints} instance created using the
     * specified properties.
     */
    @Override
    public Constraints parse(Properties db, TokenTranslator tr,
                             ParserLoader parserLoader) {
        String legalNames = tr.translate(db.getProperty("legalNames"));
        String illegalNames = tr.translate(db.getProperty("illegalNames"));
        String legalNameERE = tr.translate(db.getProperty("legalNameERE"));
        String illegalNameERE = tr.translate(db.getProperty("illegalNameERE"));
        String minName = tr.translate(db.getProperty("minName"));
        String maxName = tr.translate(db.getProperty("maxName"));
        String separator = tr.translate(db.getProperty("separator"));
        
        String valueType = db.getProperty("valueType");
        String valueConstraintsType = db.getProperty("valueConstraintsType");
        
        if (valueType == null)
            valueType = "string";
        if (valueConstraintsType == null)
            valueConstraintsType = valueType;

        //  Create the name constraint only when we need to.
        StringConstraints nameConstraints = null;
        if (minName != null || maxName != null || 
                legalNames != null || illegalNames != null ||
                legalNameERE != null || illegalNameERE != null)
            nameConstraints = 
                new StringConstraints(minName, maxName,
                                      legalNames, illegalNames,
                                      legalNameERE, illegalNameERE);

        //  Create the value parser only when we need to.
        ParameterParser valueParser = (ParameterParser) 
                parserLoader.getParser(valueType, ParameterParser.class);
        
        ConstraintsParser cp = (ConstraintsParser)
                parserLoader.getParser(valueConstraintsType,
                                       ConstraintsParser.class);
        Constraints valueConstraints = cp.parse(db, tr, parserLoader);
        
        return new NameValueConstraints(nameConstraints, valueParser,
                                        valueConstraints, separator);
    }

    /** 
     * Returns a non-null object if the parsing of the given token was
     * succesful and if the object value complied with the specified
     * constraints, if any were specified.
     *
     * @see ParameterParser#parse
     *
     * @return A validated {@link NameValuePair} instance or null if the
     * resulting name value pair does not meet the constraints.
    */
    @Override
    public Serializable parse(String token, Constraints constraints,
                              Parameters soFar) {
        //  Insiste on having constraints or bail with no match.
        if (constraints == null || 
                !(constraints instanceof NameValueConstraints))
            return null;
        NameValueConstraints nvc = (NameValueConstraints) constraints;

        //  Insist on detecting a separator character in the token.
        String separator = nvc.getSeparator();
        int i = token.indexOf(separator);
        if (i <= 0)
            return null;
        
        //  Split the token into name and value portions.
        String name = token.substring(0, i);
        String valueToken = token.substring(i + 1);

        //  Insist on the name portion being valid...
        if (nvc.getNameConstraints() != null &&
            !nvc.getNameConstraints().isValid(name))
            return null;

        //  Insist on the value portion being valid...
        Serializable value = nvc.getValueParser().
                parse(valueToken, nvc.getValueConstraints(), soFar);
        if (value == null)
            return null;
        
        //  Now construct a name/value pair and return it.
        return new NameValuePair(name, value);
    }

}
