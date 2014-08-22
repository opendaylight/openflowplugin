/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

/**
 * Argument matcher that verifies the content of a byte array argument.
 * 
 * @author Fabiel Zuniga
 */
public class ByteArrayArgumentMatcher extends ArgumentMatcher<byte[]> {

    private byte[] expectedContent;
    private int offset;

    /**
     * Creates a byte array content argument matcher
     * 
     * @param name name to display when the argument does not match
     * @param expectedContent expected content
     * @param offset offset of {@code expectedContent} in the argument
     */
    public ByteArrayArgumentMatcher(String name, byte[] expectedContent,
                                    int offset) {
        super(name);

        if (expectedContent == null) {
            throw new NullPointerException("expectedContent cannot be null");
        }

        if (offset < 0) {
            throw new IllegalArgumentException(
                                               "offset must be greater or equal to zero");
        }

        this.expectedContent = expectedContent.clone();
        this.offset = offset;
    }

    @Override
    public boolean verifyMatch(byte[] argument) {
        if (argument.length < this.expectedContent.length + this.offset) {
            return false;
        }

        for (int i = 0; i < this.expectedContent.length; i++) {
            if (argument[i + this.offset] != this.expectedContent[i]) {
                return false;
            }
        }

        return true;
    }
}
