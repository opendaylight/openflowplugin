/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.util.syntax;

import java.util.Arrays;


/**
 * Representation of a syntax match against given command-line arguments.
 *
 * @author Thomas Vachuska
 */
public class SyntaxMatch {
    
    private Parameters parameters;
    private Syntax syntax;
    private String[] args;
    private SyntaxRepository repo;

    /**
     * Creates a new syntax match with the given set of parameters and syntax
     * associated with it.
     * 
     * @param parameters parameters that are associated with this parsed
     *        syntax
     * @param syntax syntax for the associated command
     * @param repo syntax repository that produced this match
     * @param args array of the original command-line arguments being matched
     */
    public SyntaxMatch(Parameters parameters, Syntax syntax, String[] args,
                       SyntaxRepository repo) {
        this.parameters = parameters;
        this.syntax = syntax;
        this.args = Arrays.copyOf(args, args.length);
        this.repo = repo;
    }

    /**
     * Returns the parsed set of parameters associated with the match.
     * 
     * @return Returns the parsed set of parameters associated with the match.
     */
    public Parameters parameters() {
        return parameters;
    }

    /**
     * Returns the syntax associated with the match.
     * 
     * @return Returns the syntax associated with the match.
     */
    public Syntax syntax() {
        return syntax;
    }

    /**
     * Get array of the original command-line arguments that were matched.
     * 
     * @return array of the command-line arguments that were matched
     */
    public String[] args() {
        return Arrays.copyOf(args, args.length);
    }
    
    /**
     * Get the syntax repository that produced this match descriptor.
     * 
     * @return the creator syntax repository
     */
    public SyntaxRepository repo() {
        return repo;
    }

    @Override
    public String toString() {
        return "Syntax: " + syntax + ": Parameters: " + parameters;
    }

}
