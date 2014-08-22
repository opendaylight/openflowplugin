/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.text.ParseException;

/**
 * Exception representing a problem during parsing of a parameter.
 *
 * @author Thomas Vachuska
*/
public class BadUsageException extends ParseException {

    private static final long serialVersionUID = -283215999678931619L;

    /** The root syntax on which the bad usage was detected.  */
    private Syntax syntax;

    /** The syntax node that caused this exception.  */
    private SyntaxNode syntaxNode;

    /**
     * Constructs a new exception representing a keyword mismatch.
     *
     * @param message Detailed error message.
     * @param errorOffset Argument position where the mismatch occurred.
     * @param syntax {@link Syntax} entity representing the closest match.
     * @param syntaxNode {@link SyntaxNode} entity that caused the mismatch.
     */
    public BadUsageException(String message, int errorOffset,
                             Syntax syntax, SyntaxNode syntaxNode) {
        super(message, errorOffset);
        this.syntax = syntax;
        this.syntaxNode = syntaxNode;
    }

    /**
     * Get the syntax whose usage was violated.
     *
     * @return {@link Syntax} representing the closest match.
     */
    public Syntax getSyntax() {
        return syntax;
    }

    /** 
     * Get the specific syntax node which caused the parsing failure.
     *
     * @return {@link SyntaxNode} that caused the mismatch.
     */
    public SyntaxNode getSyntaxNode() {
        return syntaxNode;
    }

}
