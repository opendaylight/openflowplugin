/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.crypt;

import java.io.*;
import java.util.Properties;

/**
 * A {@link java.util.Properties} class that can handle encrypted property values in the
 * form of <pre>&quot;ENC(&lt;encoded text&gt;)&quot;</pre>. Non-encrypted
 * values do not have the <pre>&quot;ENC()&quot;</pre> magic mark.
 *
 * @author Liem Nguyen
 */
public class EncryptedProperties extends Properties {

    private static final long serialVersionUID = -2615770136967878304L;

    private transient EncryptedString es;

    /**
     * Constructor.
     *
     * @param password password to use for encryption/decryption
     * @throws EncryptionException something is wrong, Houston!
     */
    public EncryptedProperties(String password) throws EncryptionException {
        es = new EncryptedString(password);
    }

    /**
     * This also decrypts any property value that has been encrypted.
     */
    @Override
    public synchronized String getProperty(String key) {
        String val = (String) super.get(key);
        return (es.isEncrypted(val)) ? es.decrypt(val) : val;
    }

    /**
     * This also encrypts the value of any property key that was original
     * encrypted.
     */
    @Override
    public synchronized Object setProperty(String key, String value) {
        if (isEncrypted(key))
            return setEncryptedProperty(key, value);
        return super.setProperty(key, value);
    }

    /**
     * Force set a property value to be encrypted.
     *
     * @param key property key
     * @param value property value
     * @return the previous value of the specified key in this property list,
     *         or null if it did not have one.
     */
    public synchronized Object setEncryptedProperty(String key, String value) {
        return super.setProperty(key, es.encrypt(value));
    }

    /**
     * Check if the value with the given key is encrypted or not.
     *
     * @param key key with value to check
     * @return true if encrypted, false otherwise
     */
    public boolean isEncrypted(String key) {
        String val = (String) super.get(key);
        return es.isEncrypted(val);
    }

    /**
     * A simple driver for changing the master key used for encryption (and the
     * corresponding properties files that have encryption).  OR, simply
     * decrypt a given properties file or encrypt a given properties.
     *
     * @param argv command arguments <propFile|properties> <oldPass> [newPass]
     * @throws java.io.FileNotFoundException file location not found
     * @throws java.io.IOException file IO exception
     */
    public static void main(String[] argv) throws FileNotFoundException,
            IOException {
        if (argv.length < 2) {
            System.err.println("Usage: <propFile|properties> <oldPass> [newPass]");
            System.exit(1);
        }
        File propFile = new File(argv[0]);
        String pass = argv[1];

        InputStream in = null;
        OutputStream out = null;

        try {
            // change password
            if (argv.length == 3) {
                in = new FileInputStream(propFile);
                Properties p = changePass(in, pass, argv[2]);
                in.close();
                out = new FileOutputStream(propFile);
                p.store(out, null);
            } else if (!propFile.exists()) {
                // encrypt
                System.out.println(new EncryptedString(pass).encrypt(argv[0]));
            } else {
                // decrypt
                EncryptedProperties p = new EncryptedProperties(pass);
                in = new FileInputStream(propFile);
                p.load(in);
                for (String key : p.stringPropertyNames())
                    System.out.println(key + "=" + p.getProperty(key));
            }
        } catch (EncryptionException ee) {
            System.err.println("Encryption error");
            ee.printStackTrace();
            System.exit(1);
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
        }
    }

    /**
     * @param in InputStream for properties file
     * @param oldPass old password
     * @param newPass new password
     * @return new properties
     * @throws java.io.IOException cannot read InputStream
     */
    static EncryptedProperties changePass(InputStream in, String oldPass,
            String newPass) throws IOException {
        EncryptedProperties oldp = new EncryptedProperties(oldPass);
        EncryptedProperties newp = new EncryptedProperties(newPass);
        oldp.load(in);
        for (String key : oldp.stringPropertyNames())
            if (oldp.isEncrypted(key))
                newp.setEncryptedProperty(key, oldp.getProperty(key));
            else
                newp.setProperty(key, oldp.getProperty(key));
        return newp;
    }
}
