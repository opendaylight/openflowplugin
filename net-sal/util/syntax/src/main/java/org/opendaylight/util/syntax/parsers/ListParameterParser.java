/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import org.opendaylight.util.syntax.Parameters;

/**
 * Abstraction of a parser capable of producing lists of parameters.
 * 
 * @author Thomas Vachuska
 */
public abstract class ListParameterParser extends AbstractParameterParser {

    private static final long serialVersionUID = 511493699859378066L;

    /** 
     * Default constructor.
     */
    protected ListParameterParser() {
    }

    /**
     * Constructs a new parser using the given locale.
     *
     * @param locale Locale to associate with this parser.
     */
    protected ListParameterParser(Locale locale) { 
        super(locale);
    }

    /**
     * Returns the parsed object corresponding to the given list item.  This
     * method should only parse the token and does not need to invoke 
     * {@link Constraints#isValid} as this is performed by {@link #parse} 
     * method.
     *
     * @param token String to be decoded/parsed into a Serializable entity.
     * @param constraints Constraints &amp; hints to use for parsing
     * and for validating the resulting Serializable.
     * @param soFar Parameters accumulated by the parsing process thus far.
     * @return A serializable entity created based on the token value.
     */
    protected abstract Serializable parseItem(String token, 
                                              Constraints constraints, 
                                              Parameters soFar);

    /**
     * Returns a non-null List of objects if the parsing of the given tokens
     * string was succesful and if each of the list item objects complied with
     * the specified constraints, if any were specified.  Returns null
     * otherwise.
     *
     * @see ParameterParser#parse
     *
     * @return {@link java.util.ArrayList} of {@link java.io.Serializable}
     * entities if the parsing was successful; returns null otherwise.
     */
    @Override
    public Serializable parse(String tokens, Constraints constraints,
                              Parameters soFar) {
        //  Cast the constrains into ListConstraints, which is what we expect.
        //  Note that it's possible for us not to have a any constraints.
        ListConstraints lc = 
            constraints != null && constraints instanceof ListConstraints ?
            (ListConstraints) constraints : null;

        //  Extract a separator or assume a default one.
        String separator = 
            lc == null ? ListConstraints.SEPARATOR : lc.getSeparator();
        if (separator == null)
            separator = ListConstraints.SEPARATOR;

        //  Iterate over all tokens separated by the separator and add them to
        //  the result list as long as they are valid.  If you encounter an
        //  invalid one, bail with null result.
        StringTokenizer st = new StringTokenizer(tokens, separator);
        ArrayList<Serializable> results = new ArrayList<Serializable>();
        while (st.hasMoreTokens()) {
            String oneToken = st.nextToken();
            Serializable token = parseItem(oneToken, constraints, soFar);
            if (constraints != null && !constraints.isValid(token))
                return null;
            results.add(token);
        }
        
        int size = results.size();
        
        //  Check min & max requirements, unless there are no constraints.
        Integer min = lc != null ? lc.getMinLength() : null;
        Integer max = lc != null ? lc.getMaxLength() : null;
                
        if ((min == null || min.intValue() <= size) &&
            (max == null || max.intValue() >= size))
            return results;
        return null;
    }
    
}
