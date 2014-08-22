/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import org.opendaylight.util.junit.ThrowableTester.Instruction;
import org.opendaylight.util.junit.ThrowableTester.Validator;

/**
 * Test for {@link org.opendaylight.util.junit.ThrowableTester}
 * 
 * @author Fabiel Zuniga
 */
public class ThrowableTesterTest {

    @Test
    public void testAssertAnyThrowableType() {
        ThrowableTester.assertAnyThrowableType(RuntimeException.class, new ExceptionTest());
    }

    @Test
    public void testAssertAnyThrowableTypeFail() {
        try {
            ThrowableTester.assertAnyThrowableType(RuntimeException.class,
                                                   new Exception());
            // Note: Asser.fail() cannot be used because is throws an
            // AssertionError.
            throw new RuntimeException("Expected failure");
        } catch (AssertionError e) {
            String expected = getMessage(RuntimeException.class);
            String actual = getMessage(Exception.class);
            String expectedMessage = "Invalid throwable, expected any <"
                    + expected + "> but was <" + actual + ">";
            Assert.assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void testAssertThrowableType() {
        ThrowableTester.assertThrowableType(ExceptionTest.class, new ExceptionTest());
    }

    @Test
    public void testAssertThrowableTypeFail() {
        try {
            ThrowableTester.assertThrowableType(ExceptionTest.class,
                                                new Exception());
            throw new RuntimeException("Expected failure");
        } catch (AssertionError e) {
            String expected = getMessage(ExceptionTest.class);
            String actual = getMessage(Exception.class);
            String expectedMessage = "Invalid throwable, expected:<class "
                    + expected + "> but was:<class " + actual + ">";
            Assert.assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void testThrowsAny() throws Throwable {
        Throwable throwable = new ExceptionTest();
        Instruction instructionMock = EasyMock.createMock(Instruction.class);

        instructionMock.execute();
        EasyMock.expectLastCall().andThrow(throwable);

        EasyMock.replay(instructionMock);

        ThrowableTester.testThrowsAny(RuntimeException.class, instructionMock);

        EasyMock.verify(instructionMock);
    }

    @Test
    public void testThrowsAnyWithValidator() throws Throwable {
        Throwable throwable = new ExceptionTest();
        Instruction instructionMock = EasyMock.createMock(Instruction.class);
        @SuppressWarnings("unchecked")
        Validator<RuntimeException> validator = EasyMock
            .createMock(Validator.class);

        instructionMock.execute();
        EasyMock.expectLastCall().andThrow(throwable);
        validator.assertThrowable(EasyMock.same((RuntimeException) throwable));

        EasyMock.replay(instructionMock, validator);

        ThrowableTester.testThrowsAny(RuntimeException.class, instructionMock,
                                      validator);

        EasyMock.verify(instructionMock, validator);
    }

    @Test
    public void testThrowsAnyFailNoThrowable() throws Throwable {
        Instruction instructionMock = EasyMock.createMock(Instruction.class);
        instructionMock.execute();

        EasyMock.replay(instructionMock);

        try {
            ThrowableTester.testThrowsAny(RuntimeException.class,
                                          instructionMock);
            throw new RuntimeException("Expected failure");
        } catch (AssertionError e) {
            String expected = getMessage(RuntimeException.class);
            String expectedMessage = "Expected any <" + expected
                    + "> but nothing was thrown";
            Assert.assertEquals(expectedMessage, e.getMessage());
        }

        EasyMock.verify(instructionMock);
    }

    @Test
    public void testThrowsAnyFailUnexpectedThrowable() throws Throwable {
        Throwable throwable = new Exception();
        Instruction instructionMock = EasyMock.createMock(Instruction.class);

        instructionMock.execute();
        EasyMock.expectLastCall().andThrow(throwable);

        EasyMock.replay(instructionMock);

        try {
            ThrowableTester.testThrowsAny(RuntimeException.class,
                                          instructionMock);
            throw new RuntimeException("Expected failure");
        } catch (AssertionError e) {
            String expected = getMessage(RuntimeException.class);
            String actual = getMessage(Exception.class);
            String expectedMessage = "Invalid throwable, expected any <"
                    + expected + "> but was <" + actual + ">";
            Assert.assertEquals(expectedMessage, e.getMessage());
        }

        EasyMock.verify(instructionMock);
    }

    @Test
    public void testThrows() throws Throwable {
        Throwable throwable = new ExceptionTest();
        Instruction instructionMock = EasyMock.createMock(Instruction.class);

        instructionMock.execute();
        EasyMock.expectLastCall().andThrow(throwable);

        EasyMock.replay(instructionMock);

        ThrowableTester.testThrows(ExceptionTest.class, instructionMock);

        EasyMock.verify(instructionMock);
    }

    @Test
    public void testThrowsWithValidator() throws Throwable {
        Throwable throwable = new ExceptionTest();
        Instruction instructionMock = EasyMock.createMock(Instruction.class);
        @SuppressWarnings("unchecked")
        Validator<ExceptionTest> validator = EasyMock
            .createMock(Validator.class);

        instructionMock.execute();
        EasyMock.expectLastCall().andThrow(throwable);
        validator.assertThrowable(EasyMock.same((ExceptionTest) throwable));

        EasyMock.replay(instructionMock, validator);

        ThrowableTester.testThrows(ExceptionTest.class, instructionMock,
                                   validator);

        EasyMock.verify(instructionMock, validator);
    }

    @Test
    public void testThrowsFailNoThrowable() throws Throwable {
        Instruction instructionMock = EasyMock.createMock(Instruction.class);
        instructionMock.execute();

        EasyMock.replay(instructionMock);

        try {
            ThrowableTester.testThrows(RuntimeException.class, instructionMock);
            throw new RuntimeException("Expected failure");
        } catch (AssertionError e) {
            String expected = getMessage(RuntimeException.class);
            String expectedMessage = "Expected <" + expected
                    + "> but nothing was thrown";
            Assert.assertEquals(expectedMessage, e.getMessage());
        }

        EasyMock.verify(instructionMock);
    }

    @Test
    public void testThrowsFailUnexpectedThrowable() throws Throwable {
        Throwable throwable = new Exception();
        Instruction instructionMock = EasyMock.createMock(Instruction.class);

        instructionMock.execute();
        EasyMock.expectLastCall().andThrow(throwable);

        EasyMock.replay(instructionMock);

        try {
            ThrowableTester.testThrows(ExceptionTest.class, instructionMock);
            throw new RuntimeException("Expected failure");
        } catch (AssertionError e) {
            String expected = getMessage(ExceptionTest.class);
            String actual = getMessage(Exception.class);
            String expectedMessage = "Invalid throwable, expected:<class "
                    + expected + "> but was:<class " + actual + ">";
            Assert.assertEquals(expectedMessage, e.getMessage());
        }

        EasyMock.verify(instructionMock);
    }

    private static String getMessage(Class<? extends Throwable> throwableType) {
        return throwableType.getName();
    }

    private static class ExceptionTest extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
}
