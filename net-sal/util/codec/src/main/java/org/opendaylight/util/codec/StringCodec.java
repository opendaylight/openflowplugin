/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

/**
 * This interface defines methods for encoding and decoding strings to
 * an alternate form.
 *
 * @author Simon Hunt
 */
public interface StringCodec {

    /** This method takes some original string and returns it encoded in
     * a different form.
     *
     * @param original the original string
     * @return the encoded string
     */
    public String encode(String original);

    /** This method takes an encoded string and returns the decoded
     * original string.
     *
     * @param encoding the encoded string
     * @return the original string
     */
    public String decode(String encoding);

    /** Returns the number of mappings in the codec.
     *
     * @return the number of mappings.
     */
    public int size();

}
