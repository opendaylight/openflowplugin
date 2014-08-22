/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

/**
 * Exception representing a problem during compilation of syntax.
 *
 * @author Thomas Vachuska
*/
public class BadSyntaxException extends RuntimeException {

    private static final long serialVersionUID = 7668897378493052868L;
    
    /** SyntaxNode that caused this exception.  */
    private SyntaxNode syntaxNode;

    /**
     * Constructs a new exception representing a keyword mismatch.
     *
     * @param message Detailed error message.
     * @param syntaxNode {@link SyntaxNode} entity that caused the failure.
     */
    public BadSyntaxException(String message, SyntaxNode syntaxNode) {
        super(message);
        this.syntaxNode = syntaxNode;
    }

    /** 
     * Get the specific syntax node which caused the compilation failure.
     *
     * @return {@link SyntaxNode} that caused the compilation error.
     */
    public SyntaxNode getSyntaxNode() {
        return syntaxNode;
    }

}
