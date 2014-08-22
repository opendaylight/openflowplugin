/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * This JUnit test class tests the AbstractValidator class.
 *
 * @author Simon Hunt
 */
public class AbstractValidatorTest {

    // concrete implementation
    private static class MyValidator extends AbstractValidator {

    }

    private static final Throwable CAUSE = new RuntimeException("A Good Cause");
    
    private static final String MSG_1 = "Message One";
    private static final String MSG_2 = "Message Two";
    private static final String MSG_3 = "Message Three";
    private static final String BAD_MSG = "Something bad happened";

    private AbstractValidator av;

    @Before
    public void setUp() {
        av = new MyValidator();
    }


    @Test
    public void basic() {
        print(EOL + "basic()");
        print("[" + av.getMessagesAsString() + "]");
        assertEquals(AM_UXS, 0, av.getMessages().size());
        assertEquals(AM_UXS, 0, av.numberOfMessages());
    }

    @Test
    public void someMessages() {
        print(EOL + "someMessages()");
        av.addInfo(MSG_1);
        av.addWarning(MSG_2);
        av.addError(MSG_3);
        print("[" + av.getMessagesAsString() + "]");
        assertEquals(AM_UXS, 3, av.numberOfMessages());
        assertEquals(AM_UXS, av.numberOfMessages(), av.getMessages().size());

        String allMsgs = av.getMessagesAsString();
        assertTrue(AM_HUH, allMsgs.contains(MSG_1));
        assertTrue(AM_HUH, allMsgs.contains(MSG_2));
        assertTrue(AM_HUH, allMsgs.contains(MSG_3));
    }

    @Test
    public void infoMessages() {
        assertEquals(AM_UXS, 0, av.numberOfMessages());
        av.addInfo(MSG_1);
        assertEquals(AM_UXS, 1, av.numberOfMessages());
        assertEquals(AM_NEQ, AbstractValidator.INFO + MSG_1, av.getMessages().get(0));
    }

    @Test
    public void warningMessages() {
        assertEquals(AM_UXS, 0, av.numberOfMessages());
        av.addWarning(MSG_2);
        assertEquals(AM_UXS, 1, av.numberOfMessages());
        assertEquals(AM_NEQ, AbstractValidator.WARNING + MSG_2, av.getMessages().get(0));
    }

    @Test
    public void errorMessages() {
        assertEquals(AM_UXS, 0, av.numberOfMessages());
        av.addError(MSG_3);
        assertEquals(AM_UXS, 1, av.numberOfMessages());
        assertEquals(AM_NEQ, AbstractValidator.ERROR + MSG_3, av.getMessages().get(0));
    }

    @Test
    public void verbatimMessages() {
        assertEquals(AM_UXS, 0, av.numberOfMessages());
        av.addVerbatim(MSG_2);
        assertEquals(AM_UXS, 1, av.numberOfMessages());
        assertEquals(AM_NEQ, MSG_2, av.getMessages().get(0));
    }

    @Test
    public void throwingExceptionIfMessages() {
        print(EOL + "throwingExceptions()");
        assertEquals(AM_UXS, 0, av.numberOfMessages());
        try {
            av.throwExceptionIfMessages();
        } catch (ValidationException ve) {
            fail("Should not have thrown an exception here");
        }

        av.addError(MSG_1);
        av.addError(MSG_2);

        try {
            av.throwExceptionIfMessages();
            fail(AM_NOEX);
        } catch (ValidationException ve) {
            assertEquals(AM_NEQ, av.getMessages(), ve.getIssues());
            print (ve);
        }
    }

    @Test
    public void throwExceptionWithCustomMessage() {
        try {
            av.throwException(BAD_MSG);
            fail(AM_NOEX);
        } catch (ValidationException ve) {
            assertEquals(AM_NEQ, BAD_MSG, ve.getMessage());
        }
    }
    @Test
    public void throwExceptionWithCustomMessageAndCause() {
        try {
            av.throwException(BAD_MSG, CAUSE);
            fail(AM_NOEX);
        } catch (ValidationException ve) {
            assertEquals(AM_NEQ, BAD_MSG, ve.getMessage());
            assertEquals(AM_NEQ, CAUSE, ve.getCause());
        }
    }
}
