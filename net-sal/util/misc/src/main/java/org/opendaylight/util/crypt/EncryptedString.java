/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.crypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEParameterSpec;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

/**
 * A utility to help with encrypting and decrypting string values.
 *
 * @author Liem Nguyen
 */
public class EncryptedString {    

    private static final byte[] SALT = { (byte) 0x01, (byte) 0x09, (byte) 0x07,
            (byte) 0x04, (byte) 0x04, (byte) 0x07, (byte) 0x09, (byte) 0x01};
    
    private static final int SALT_ITERATIONS = 20;

    private static final String ENC_PREFIX = "ENC(";

    private static final String ENC_POSTFIX = ")";
    
    public static final String EMPTY = ENC_PREFIX + ENC_POSTFIX;

    private Cipher encrypter, decrypter;

    /**
     * Constructor.
     * 
     * @param password password used for encryption and decryption
     * @throws EncryptionException crypto initialization exception
     */
    public EncryptedString(String password) throws EncryptionException {
        PBEParameterSpec ps = new PBEParameterSpec(SALT, SALT_ITERATIONS);
        try {
            SecretKeyFactory kf = SecretKeyFactory
                    .getInstance("PBEWithSHA1AndDESede");
            SecretKey k = kf.generateSecret(new javax.crypto.spec.PBEKeySpec(
                    password.toCharArray()));
            encrypter = Cipher.getInstance("PBEWithSHA1AndDESede/CBC/PKCS5Padding");
            decrypter = Cipher.getInstance("PBEWithSHA1AndDESede/CBC/PKCS5Padding");
            encrypter.init(Cipher.ENCRYPT_MODE, k, ps);
            decrypter.init(Cipher.DECRYPT_MODE, k, ps);
        } catch (Throwable t) {
            throw new EncryptionException(t);
        }
    }
    
    /**
     * Check if a string is encrypted or not.
     * 
     * @param str string to check
     * @return true if encrypted, false otherwise
     */
    public boolean isEncrypted(String str) {
        return (str != null) && str.startsWith(ENC_PREFIX)
                && str.endsWith(ENC_POSTFIX);
    }

    /**
     * Decrypt a given string.  The string to be decrypted is assumed to have 
     * passed the {@link #isEncrypted(String)} check with a <pre>true</pre> 
     * return value.
     * 
     * @param str string to decrypt
     * @return decrypted string
     * @throws EncryptionException decryption error
     */
    public synchronized String decrypt(String str) throws EncryptionException {
        try {
            String val = stripCrypt(str);
            byte[] dec = decodeBase64(val);
            byte[] utf8 = decrypter.doFinal(dec);
            return new String(utf8, "UTF-8");
        } catch (Throwable t) {
            throw new EncryptionException("Decryption error", t);
        }
    }

    /**
     * Encrypt a given string.
     * 
     * @param str string to be encrypted
     * @return encrypted string
     * @throws EncryptionException encryption error
     */
    public synchronized String encrypt(String str) throws EncryptionException {
        try {
            byte[] utf8 = str.getBytes("UTF-8");
            byte[] enc = encrypter.doFinal(utf8);
            return addCrypt(encodeBase64String(enc));
        } catch (Throwable t) {
            throw new EncryptionException("Encryption error", t);
        }
    }

    private static String addCrypt(String str) {
        return ENC_PREFIX + str + ENC_POSTFIX;
    }
    
    private static String stripCrypt(String str) {
        return str.substring(4, str.length() - 1);
    }
}
