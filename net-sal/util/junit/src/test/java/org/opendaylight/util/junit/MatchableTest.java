/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import java.io.PrintStream;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import org.opendaylight.util.junit.ThrowableTester.Instruction;

/**
 * {@link org.opendaylight.util.junit.Matchable} tests.
 * 
 * @author Fabiel Zuniga
 */
public class MatchableTest {

    @Test
    public void testConstruction() {
        Matchable<String> matchedPropertyA = Matchable.valueOf("matchedPropertyA", "value", "value");
        Matchable<String> matchedPropertyB = Matchable.valueOf("matchedPropertyB", null, null);
        Matchable<String> mismatchedPropertyA = Matchable.valueOf("mismatchedPropertyA", "value", "different value");
        Matchable<String> mismatchedPropertyB = Matchable.valueOf("mismatchedPropertyB", null, "different value");
        Matchable<String> mismatchedPropertyC = Matchable.valueOf("mismatchedPropertyB", "value", null);

        Assert.assertTrue(matchedPropertyA.matches());
        Assert.assertTrue(matchedPropertyB.matches());
        Assert.assertFalse(mismatchedPropertyA.matches());
        Assert.assertFalse(mismatchedPropertyB.matches());
        Assert.assertFalse(mismatchedPropertyC.matches());
    }

    @Test
    public void testInvalidConstruction() {
        final String invalidNameNull = null;
        final String invalidNameEmpty = "";

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                Matchable.valueOf(invalidNameNull, null, null);
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                Matchable.valueOf(invalidNameEmpty, null, null);
            }
        });
    }

    @Test
    public void testPrintMismatch() {
        @SuppressWarnings("resource")
        PrintStream streamMock = EasyMock.createMock(PrintStream.class);

        final String name = "property name";
        final String expected = "property value";
        final String actual = "property different value";

        streamMock.println(EasyMock.contains(name));
        streamMock.println(EasyMock.contains(expected));
        streamMock.println(EasyMock.contains(actual));

        EasyMock.replay(streamMock);

        Matchable<String> matchable = Matchable.valueOf(name, expected, actual);

        matchable.printMismatch(streamMock);

        EasyMock.verify(streamMock);
    }
}
