/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import org.easymock.IArgumentMatcher;

/**
 * Argument matcher.
 * <p>
 * This argument matcher already verifies null-ability and type: If the
 * argument is {@code null} or of the wrong type the match fails.
 * 
 * @param <T> type of the expected argument
 * @author Fabiel Zuniga
 */
public abstract class ArgumentMatcher<T> implements IArgumentMatcher {

    private String name;

    /**
     * Creates an argument matcher.
     * 
     * @param name name to display when the argument does not match
     */
    public ArgumentMatcher(String name) {
        this.name = name;
    }

    @Override
    public void appendTo(StringBuffer buffer) {
        buffer.append("<" + this.name + ">");
    }

    @Override
    public boolean matches(Object arg) {
        if (arg == null) {
            return false;
        }

        try {
            @SuppressWarnings("unchecked")
            T argument = (T) arg;
            return verifyMatch(argument);
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Verifies the argument matches.
     * 
     * @param argument argument
     * @return {@code true} if {@code argument} matches expectations,
     *         {@code false} otherwise
     */
    public abstract boolean verifyMatch(T argument);

    /**
     * Verifies whether the properties match the expected values.
     * 
     * @param properties properties to verify
     * @return {@code true} if the property's value matches the expected,
     *         {@code false} otherwise
     */
    @SafeVarargs
    protected final boolean verify(Matchable<?>... properties) {
        for (Matchable<?> property : properties) {
            if (!property.matches()) {
                System.err.println("Property mismatch: " + getClass().getName()
                        + "<" + this.name + ">");
                property.printMismatch(System.err);
                return false;
            }
        }
        return true;
    }
}