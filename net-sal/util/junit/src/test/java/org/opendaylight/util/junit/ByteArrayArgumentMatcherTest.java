/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import org.easymock.EasyMock;
import org.junit.Test;

import org.opendaylight.util.junit.ThrowableTester.Instruction;

/**
 * {@link org.opendaylight.util.junit.ByteArrayArgumentMatcher} tests.
 * 
 * @author Fabiel Zuniga
 */
public class ByteArrayArgumentMatcherTest {

    @Test
    public void testInvalidConstruction() {
        final byte[] validExpectedContent = new byte[1];
        final byte[] invalidExpectedContent = null;

        final int validOffset = 0;
        final int invalidOffset = -1;

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @SuppressWarnings("unused")
            @Override
            public void execute() throws Throwable {
                new ByteArrayArgumentMatcher(null, invalidExpectedContent, validOffset);
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @SuppressWarnings("unused")
            @Override
            public void execute() throws Throwable {
                new ByteArrayArgumentMatcher(null, validExpectedContent, invalidOffset);
            }
        });
    }

    @Test
    public void testVerifyMatch() {
        byte[] argument = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5 };
        byte[] expectedContent = new byte[] { (byte) 2, (byte) 3, (byte) 4 };

        Mockable mockable = EasyMock.createMock(Mockable.class);

        ArgumentMatcher<byte[]> matcher = new ByteArrayArgumentMatcher("MyArgumentMatcher", expectedContent, 1);
        mockable.myMethod(EasyMockArgumentMatcher.match(matcher));

        EasyMock.replay(mockable);
        mockable.myMethod(argument);
        EasyMock.verify(mockable);
    }

    private static interface Mockable {
        public void myMethod(byte[] argument);
    }
}
