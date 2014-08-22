/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api.security;

/**
 * Encapsulates TLS based crypt parameters, identifying the keystore and
 * truststore (and their passwords) to be used in setting up secure connections.
 * <p>
 * The keystore and truststore files may be generated using the JAVA
 * {@code keytool} command. The root CA and reply certificates can be
 * imported into keystore file or can create different truststore file.
 * <p>
 * Example key and certificate management:
 * <ul>
 *      <li>
 *          Generate Key pair
 * <pre>
 * keytool -genkey -alias server -keyalg RSA -keysize 2048 \
 *   -keystore controller.jks \
 *   -dname "CN=controller,OU=HPN, O=HP, L=Roseville, ST=CA, C=US"
 * </pre>
 *      </li>
 *
 *      <li>
 *          Generate CSR(Certificate Signing Request) from key pair
 * <pre>
 * keytool -certreq -alias server -file controller.csr \
 *   -keystore controller.jks
 * </pre>
 *      </li>
 *
 *      <li>
 *          Submit CSR to CA (Certification Authority) for signing. The CA will
 *          authenticate and then will return a certificate signed by them,
 *          authenticating submitted Public Key. The CA may return a chain of
 *          certificates also.
 *          (The CA will return root CA and certificate reply)
 *          <pre></pre>
 *       </li>
 *
 *      <li>
 *          Import the root certificate for the CA.
 * <pre>
 * keytool -importcert -trustcacerts -keystore controller.jks \
 *   -file root.pem -alias theCARoot -keypass skyline -storepass skyline
 * </pre>
 *      </li>
 *
 *      <li>
 *          Import the certificate reply from the CA.
 * <pre>
 *  keytool -importcert -trustcacerts -keystore controller.jks \
 *    -file controller.pem -alias server -keypass skyline -storepass skyline
 * </pre>
 *      </li>
 * </ul>
 *
 * @author Sudheer Duggisetty
 * @author Simon Hunt
 */
public class SecurityContext {
    private final String ksName;
    private final String ksPass;
    private final String tsName;
    private final String tsPass;

    /**
     * Constructs a crypt context with the specified arguments. Any
     * {@code null} arguments are quietly replaced with an empty string.
     *
     * @param keyStoreName the pathname of the keystore
     * @param keyStorePass the keystore password
     * @param trustStoreName the pathname of the truststore
     * @param trustStorePass the truststore password
     */
    public SecurityContext(String keyStoreName, String keyStorePass,
                           String trustStoreName, String trustStorePass) {
        this.ksName = nullIsEmpty(keyStoreName);
        this.ksPass = nullIsEmpty(keyStorePass);
        this.tsName = nullIsEmpty(trustStoreName);
        this.tsPass = nullIsEmpty(trustStorePass);
    }

    private String nullIsEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * Constructs a null crypt context; that is all four parameters are
     * empty strings.
     */
    public SecurityContext() {
        this("", "", "", "");
    }

    @Override
    public String toString() {
        return "{ks=\"" + ksName + "\"[\"" + ksPass +
                "\"], ts=\"" + tsName + "\"[\"" + tsPass + "\"]}";
    }

    /**
     * Returns the pathname of the keystore.
     *
     * @return the pathname of the keystore
     */
    public String keyStoreName() {
        return ksName;
    }

    /**
     * Returns the keystore password.
     *
     * @return the keystore password
     */
    public String keyStorePass() {
        return ksPass;
    }

    /**
     * Returns the pathname of the truststore.
     *
     * @return the pathname of the truststore
     */
    public String trustStoreName() {
        return tsName;
    }

    /**
     * Returns the truststore password.
     *
     * @return the truststore password
     */
    public String trustStorePass() {
        return tsPass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SecurityContext that = (SecurityContext) o;
        return ksName.equals(that.ksName) && ksPass.equals(that.ksPass) &&
                tsName.equals(that.tsName) && tsPass.equals(that.tsPass);
    }

    @Override
    public int hashCode() {
        int result = ksName.hashCode();
        result = 31 * result + ksPass.hashCode();
        result = 31 * result + tsName.hashCode();
        result = 31 * result + tsPass.hashCode();
        return result;
    }
}
