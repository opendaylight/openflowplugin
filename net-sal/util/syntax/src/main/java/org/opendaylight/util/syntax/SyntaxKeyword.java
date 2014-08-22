/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.util.Locale;
import java.text.ParsePosition;

import org.opendaylight.util.syntax.parsers.StringSet;

/** 
 * Class representing a leaf syntax keyword.
 *
 *  @author Thomas Vachuska
 */
public class SyntaxKeyword extends SyntaxNode {

    private static final long serialVersionUID = -6988896012409064728L;

    /** Name of this syntax element.  */
    private StringSet stringSet;

    /** 
     * Default constructor.
     */
    protected SyntaxKeyword() {
    }

    /** 
     * Constructs a new syntax keyword from the given string set token.
     *
     * @param tokens String set tokens delimited by pipe (|) character and
     * using asterisk (*) character as the abbreviation delimiter.
     * @param container Node which contains this keyword definition.
     * @param locale Locale context for this keyword node.
     */
    public SyntaxKeyword(String tokens, SyntaxNode container, Locale locale) {
        super(container, null, locale);
        this.stringSet = new StringSet(container.translate(tokens), 
                                       StringSet.VALUE_DELIMITER,
                                       StringSet.ABBREV_DELIMITER);
        setName(getTokens().getName());
        setDescription(getTokens().getName());
    }

    /** 
     * Get the enclosed StringSet which defines valid values for this keyword.
     *
     * @return {@link org.opendaylight.util.syntax.parsers.StringSet} instance that
     * defines valid values for this keyword.
     */
    public StringSet getTokens() {
      return stringSet;
    }

    /** 
     * Returns the string image of this node.
     */
    @Override
    public String toString() {
        return getTokens().getName();
    }


    /**
     * Returns true if the node matches the given arguments starting with the
     * specified position.  If a match is found, the position will be updated
     * to the next unparsed argument.  If no match is found the
     * <code>farthestMismatchPosition</code> parameter will hold the index of
     * the farthest argument that failed to match and the reference to the
     * syntax node against which that match failed.
     *
     * @see org.opendaylight.util.syntax.SyntaxNode#matches
     *
     */
    @Override
    public boolean matches(String args[], Parameters parameters, 
                           Parameters committedParameters,
                           ParsePosition position, ParsePosition start,
                           SyntaxPosition farthestMismatchPosition,
                           boolean indexParameters) {
        int i = start.getIndex();
        int j = position.getIndex();
        
        if (j >= args.length)
            return false;
        
        //  When comparing switches we want to be case-sensitive, but when
        //  comparing other keywords we want to be case-insensitive ...
        String toCompare = args[j];
        boolean result = getTokens().contains(toCompare);
        
        if (result) {
            start.setIndex(i + 1);
            position.setIndex(j + 1);
        } else {
            farthestMismatchPosition.update(position, this);
        }
        return result;
    }
    
}
