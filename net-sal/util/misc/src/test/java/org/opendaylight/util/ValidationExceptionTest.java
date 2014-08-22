/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

import java.util.Arrays;
import java.util.List;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * This JUnit test class tests the ValidationException class.
 *
 * @author Simon Hunt
 * 
 * @noinspection ThrowableInstanceNeverThrown
 */
public class ValidationExceptionTest {

    private static final String MSG = "Some message";

    private static final List<String> ISSUES = Arrays.asList(
            "First issue", "Second issue", "Third issue"
    );

    @Test
    public void constructNoIssues() {
        ValidationException ve = new ValidationException(MSG, (List <String>)null);
        print(ve);
        assertEquals(AM_NEQ, MSG, ve.getMessage());
    }

    @Test
    public void constructWithIssues() {
        ValidationException ve = new ValidationException(MSG, ISSUES);
        print(ve);
        assertEquals(AM_NEQ, MSG, ve.getMessage());
        assertEquals(AM_NEQ, ISSUES, ve.getIssues());
    }

    @Test
    public void constructWithIssuesAndCause() {
        final Throwable cause = new NullPointerException("this is the cause");
        ValidationException ve = new ValidationException(MSG, ISSUES, cause);
        print(ve);
        assertEquals(AM_NEQ, MSG, ve.getMessage());
        assertEquals(AM_NEQ, ISSUES, ve.getIssues());
        assertEquals(AM_NEQ, cause, ve.getCause());
    }

    @Test
    public void constructWithValidatorInstance() {
        try {
            FixtureAlwaysFailingValidator.validateImplementation(MSG, ISSUES);
            fail(AM_NOEX);
        } catch (ValidationException ve) {
            assertEquals(AM_NEQ, MSG, ve.getMessage());
            assertEquals(AM_NEQ, ISSUES, ve.getIssues());
        }
    }

    @Test
    public void constructWithValidatorInstanceAndCause() {
        final Throwable cause = new NullPointerException("this is the cause");
        try {
            FixtureAlwaysFailingValidator.validateImplementation(MSG, ISSUES, cause);
            fail(AM_NOEX);
        } catch (ValidationException ve) {
            assertEquals(AM_NEQ, MSG, ve.getMessage());
            assertEquals(AM_NEQ, ISSUES, ve.getIssues());
            assertEquals(AM_NEQ, cause, ve.getCause());
        }
    }

}
