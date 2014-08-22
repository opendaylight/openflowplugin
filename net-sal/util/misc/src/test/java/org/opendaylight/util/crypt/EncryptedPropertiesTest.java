/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.crypt;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * @author Liem Nguyen
 */
public class EncryptedPropertiesTest {

    private static final String PASS = "skyline";

    @Test
    public void test() throws IOException {
        EncryptedProperties p1 = new EncryptedProperties(PASS);
        p1.setProperty("noncrypt", "hello");
        p1.setEncryptedProperty("crypt", "world");

        // test in memory
        assertTrue(p1.isEncrypted("crypt"));
        assertFalse(p1.isEncrypted("noncrypt"));
        assertEquals("hello", p1.getProperty("noncrypt"));
        assertEquals("world", p1.getProperty("crypt"));

        // test modify
        for (String key : p1.stringPropertyNames()) {
            String val = p1.getProperty(key);
            p1.setProperty(key, val + "2");
        }
        assertTrue(p1.isEncrypted("crypt"));
        assertFalse(p1.isEncrypted("noncrypt"));
        assertEquals("hello2", p1.getProperty("noncrypt"));
        assertEquals("world2", p1.getProperty("crypt"));

        // test in storage/load
        File cryptFile = File.createTempFile("crypt", ".properties");
        cryptFile.deleteOnExit();
        p1.store(new FileWriter(cryptFile), "testing");

        EncryptedProperties p2 = new EncryptedProperties(PASS);
        p2.load(new FileReader(cryptFile));
        assertTrue(p2.isEncrypted("crypt"));
        assertFalse(p1.isEncrypted("noncrypt"));
        assertEquals("hello2", p2.getProperty("noncrypt"));
        assertEquals("world2", p2.getProperty("crypt"));
    }

    @Test
    public void testChangepass() throws IOException {
        InputStream in = null;
        try {
            in = EncryptedPropertiesTest.class.getClassLoader()
                    .getResourceAsStream("encrypted.properties");
            EncryptedProperties p = EncryptedProperties.changePass(in,
                    "skyl1ne", "skyl2ne");
            assertEquals("skyline", p.getProperty("plain"));
            assertEquals("skyline", p.getProperty("crypt"));
            assertTrue(p.isEncrypted("crypt"));
            assertFalse(p.isEncrypted("plain"));
        } finally {
            if (in != null)
                in.close();
        }
    }
}
