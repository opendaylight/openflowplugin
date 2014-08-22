/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

import java.util.List;

/**
 * A unit test fixture used in the testing of AbstractValidator
 *  and ValidationException.
 *
 * @author Simon Hunt
 */
public class FixtureAlwaysFailingValidator extends AbstractValidator {

    private void validate(final String msg, final List<String> issues) {
        for (String s: issues) {
            addVerbatim(s);
        }
        throwException(msg);
    }

    private void validate(final String msg, final List<String> issues, final Throwable cause) {
        for (String s: issues) {
            addVerbatim(s);
        }
        throwException(msg, cause);
    }

    //=== Public API ========

    public static void validateImplementation(final String msg, final List<String> issues) {
        new FixtureAlwaysFailingValidator().validate(msg, issues);
    }

    public static void validateImplementation(final String msg, final List<String> issues,
                                              final Throwable cause) {
        new FixtureAlwaysFailingValidator().validate(msg, issues, cause);
    }
}
