/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.util.Comparator;
import java.util.Locale;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;

import java.text.MessageFormat;
import java.text.ParsePosition;

/**
 * Matches command-line arguments against a pre-compiled set of syntaxes.
 *
 * @author Thomas Vachuska
 */
public class SyntaxMatcher {

    private ResourceBundle res = null;
    private Locale locale = Locale.US;
    private Set<SyntaxPackage> packages = new HashSet<SyntaxPackage>();
    private List<Syntax> syntaxes = new ArrayList<Syntax>();
    private Comparator<Syntax> prioritizer = new SyntaxPrioritizer();

    /**
     * Constructs the syntax compiler in the context of the given locale.
     *
     * @param locale Locale context to use for matching the otherwise
     * locale-independent syntax.
     */
    public SyntaxMatcher(Locale locale) {
        this.locale = locale;
        this.res = ResourceBundle.getBundle("org.opendaylight.util.syntax.SyntaxMatcher", locale);
    }

    /**
     * Returns the locale with which this matcher is associated.
     * 
     * @return matcher locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Registers the given set of packages with this matcher.
     * 
     * @param syntaxPackage syntax package to be added
     */
    public synchronized void addPackage(SyntaxPackage syntaxPackage) {
        this.packages.add(syntaxPackage);

        synchronized (syntaxes) {
            syntaxes.addAll(syntaxPackage.getSyntaxes());
            //  Re-prioritize the ordered list of syntaxes.
            Collections.sort(syntaxes, prioritizer);
        }
    }

    /**
     * Registers the given set of packages with this matcher.
     * 
     * @param packages set of syntax packages to be added
     */
    public void addPackages(Set<SyntaxPackage> packages) {
        Iterator<SyntaxPackage> it = packages.iterator();
        while (it.hasNext())
            addPackage(it.next());
    }

    /**
     * Returns an unmodifiable copy of the set of registered packages.
     * 
     * @return set of registered syntax packages
     */
    public Set<SyntaxPackage> getPackages() {
        return Collections.unmodifiableSet(packages);
    }


    /**
     * Returns an unmodifiable copy of the prioritized syntax list.
     * 
     * @return list of registered syntaxes
     */
    public List<Syntax> getSyntaxes() {
        return Collections.unmodifiableList(syntaxes);
    }


    /**
     * Matches command-line arguments against pre-compiled set of syntaxes.
     *
     * @param args Command-line arguments to be parsed.
     * @param parameters Parameter map to be populated (on success) with
     * string name to serializable object bindings.  syntax definitions.
     * @return {@link Syntax} instance that successfully matched the specified
     * command-line arguments
     * @throws BadUsageException thrown if the specified arguments failed to
     * match any of the syntaxes registered with this matcher.
     */
    public Syntax getMatchingSyntax(String[] args, Parameters parameters) 
                            throws BadUsageException {
        Parameters parametersFromSyntax = new Parameters();
        ParsePosition position = new ParsePosition(0);
        ParsePosition start = new ParsePosition(0);
        SyntaxPosition farthestMismatchPosition = new SyntaxPosition();
        Syntax syntax = null;
        Syntax mostValidSyntax = null;

        synchronized (syntaxes) {
            //  Iterate over all syntaxes in order of priority.
            Iterator<Syntax> it = getSyntaxes().iterator();
            while (it.hasNext()) {
                syntax = it.next();
                position.setIndex(0);
                start.setIndex(0);
                parametersFromSyntax.clear();
                parameters.clear();
                
                if (syntax.matches(args, parameters, parametersFromSyntax,
                                   position, start, farthestMismatchPosition,
                                   false)) {
                    // Woop!  We have a match!  Augment parameters and bail out.
                    return syntax;
                } else if (!syntax.isAuxiliary()
                           && farthestMismatchPosition.wasUpdated()) {
                    mostValidSyntax = syntax;
                }
            }
        }
            
        //  No luck, no match at all.
        parameters.clear();
        int errorIndex = farthestMismatchPosition.getIndex();
        SyntaxNode syntaxNode = farthestMismatchPosition.getSyntaxNode();
        String reason;
        
        //  Format the bad-usage exception reason... 
        if (args.length > 0 && errorIndex >= 0) {
            reason = (errorIndex < args.length) ? 
                MessageFormat.format(res.getString("bad_arg"), 
                                     new Object[]{ args[errorIndex],
                                                   Integer.valueOf(errorIndex+1) })
                :
                res.getString("no_guess");
        } else {
            reason = res.getString("no_args");
        }
        
        //  And complain to the caller.
        throw new BadUsageException(reason, errorIndex,
                                    mostValidSyntax, syntaxNode);
    }

}
