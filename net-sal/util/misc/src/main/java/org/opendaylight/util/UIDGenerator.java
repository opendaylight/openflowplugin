/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.security.SecureRandom;

/**
 * A simple UID generator that creates a sequence of N random characters.
 * 
 * @author Liem M. Nguyen
 * @author Thomas Vachuska
 */
public final class UIDGenerator {

    private static final char[] CHAR_POOL = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' 
    };
    
    private static SecureRandom random = new SecureRandom();

    // no instantiation
    private UIDGenerator() { }
    
    /**
     * Generate a new UID.
     * 
     * @return new 16 character UID
     */
    public static String newUID() {
        return getRandomStr(16);
    }
        
    /**
     * Generates a random string of the prescribed length.
     * 
     * @param length length of the random string to be generated
     * @return string of {@code length} random characters
     */
    private static String getRandomStr(int length) {
        char[] randomStr = new char[length];
        for (int x = 0; x < length; x++)
            randomStr[x] = CHAR_POOL[random.nextInt(CHAR_POOL.length)];
        return new String(randomStr);
    }
    
}
