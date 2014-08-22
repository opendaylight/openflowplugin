/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.crypt;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertEquals;

/**
 * @author Liem Nguyen
 */
public class MasterKeyTest {

    @Test
    public void test() {
        EncryptedString es = new EncryptedString(MasterKey.key());
        
        print(es.encrypt("skyline"));
        print(es.encrypt("AuroraSdnToken37"));
        print(es.encrypt("ADMIN"));
        
        assertEquals("skyline", es.decrypt(es.encrypt("skyline")));
    }

}
