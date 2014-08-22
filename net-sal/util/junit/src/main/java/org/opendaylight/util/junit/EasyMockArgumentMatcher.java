/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;

/**
 * Helper class to register custom argument matchers in EasyMock.
 * <P>
 * Example:
 * 
 * <pre>
 * IArgumentMatcher customArgumentMatcher = new IArgumentMatcher() {
 *     ...
 * };
 * 
 * // or even better
 * 
 * ArgumentMatcher&lt;MyArgumentType&gt; customArgumentMatcher = new ArgumentMatcher&lt;MyArgumentType&gt;() {
 *     ...
 * };
 * 
 * // For methods returning a value
 * EasyMock.expect(mock.someMethod(ArgumentMatcherSubscriber.match(customArgumentMatcher))).andReturn(...);
 * 
 * // For void return methods
 * mock.someMethod(ArgumentMatcherSubscriber.match(customArgumentMatcher));
 * </pre>
 * 
 * @author Fabiel Zuniga
 */
public final class EasyMockArgumentMatcher {

	private EasyMockArgumentMatcher() {

	}

    /**
     * Registers an argument matcher.
     * <p>
     * See {@link #match(ArgumentMatcher)} for the preferred way.
     * 
     * @param <T> type of the argument.
     * @param matcher matcher to register.
     * @return <code>null</code>.
     */
	public static <T> T match(IArgumentMatcher matcher) {
		if (matcher == null) {
			throw new NullPointerException("matcher cannot be null");
		}

		EasyMock.reportMatcher(matcher);

		return null;
	}

    /**
     * Registers an argument matcher.
     * 
     * @param <T> type of the argument
     * @param matcher matcher to register
     * @return <code>null</code>
     */
    public static <T> T match(ArgumentMatcher<T> matcher) {
        if (matcher == null) {
            throw new NullPointerException("matcher cannot be null");
        }

        EasyMock.reportMatcher(matcher);

        return null;
    }
}
