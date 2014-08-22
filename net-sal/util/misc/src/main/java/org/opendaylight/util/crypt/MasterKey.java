/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.crypt;


/**
 * A utility class to load the master key for encryption / decryption.
 *
 * @author Liem Nguyen
 */
public class MasterKey {
    
    private static String key;
    static {
        key = System.getenv("HP_KEY");
        if (key == null || key.isEmpty()) {
            key = "skyl1ne";
        }
    }
    
    private MasterKey() {}
    
    /**
     * @return the master key
     */
    public static String key() { return key; }
}
