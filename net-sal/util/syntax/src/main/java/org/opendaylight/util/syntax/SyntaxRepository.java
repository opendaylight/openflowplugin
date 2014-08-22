/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.util.syntax;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import org.opendaylight.util.syntax.usage.SyntaxUsage;

/**
 * Aggregation facility for discovering and compiling available syntax
 * packages and for matching command-line arguments against their syntax sets.
 * 
 * @author Thomas Vachuska
 */
public class SyntaxRepository {

    private Locale locale = Locale.getDefault();

    private SyntaxCompiler compiler;
    private SyntaxMatcher matcher;
    private SyntaxUsage usage;

    /**
     * Create a syntax repository using all syntaxes found ...
     */
    public SyntaxRepository() {
        compiler = new SyntaxCompiler();
        matcher = new SyntaxMatcher(locale);
        usage = new SyntaxUsage(locale);
    }

    /**
     * Gets the set of all discovered syntax packages.
     * 
     * @return set of discovered syntax packages
     */
    public Set<SyntaxPackage> packages() {
        return matcher.getPackages();
    }

    /**
     * Parses the command-line syntax definitions supplied through the given
     * input stream and adds the resulting syntax package to the repository.
     * 
     * @param is input stream containing the XML definition of command-line
     *        syntax
     * @throws IOException if issues are encountered reading from the given
     *         input stream
     */
    public void addSyntaxDefinitions(InputStream is) throws IOException {
        try {
            SyntaxPackage sp = compiler.compile(is, locale);
            matcher.addPackage(sp);
            usage.addPackage(sp);
        } catch (Exception e) {
            throw new IOException("Unable to process syntax definitions", e);
        }
    }
    
    /**
     * Matches an array of string arguments that were present on the CLIF
     * client's arguments list, and attempts to match them against a defined
     * syntax. If no syntax can be found a BadUsageException will be thrown.
     * 
     * @param args arguments that have been pulled from a the CLIF client's
     *        execution of a command line command
     * @return syntax match descriptor that contains the syntax and parameters
     *         that resulted from the successful match
     * @throws BadUsageException if the given arguments do not match with any
     *         defined syntax
     */
    public SyntaxMatch match(String args[]) throws BadUsageException {
        Parameters parameters = new Parameters();
        Syntax syntax = matcher.getMatchingSyntax(args, parameters);
        return new SyntaxMatch(parameters, syntax, args, this);
    }

    /**
     * Get the syntax usage utility instance
     * 
     * @return {@link org.opendaylight.util.syntax.usage.SyntaxUsage} utility
     */
    public SyntaxUsage syntaxUsage() {
        return usage;
    }
    
    /**
     * Get the syntax compiler utility instance
     * 
     * @return syntax compiler utility in use by the syntax repository
     */
    public SyntaxCompiler compiler() {
        return compiler;
    }

}
