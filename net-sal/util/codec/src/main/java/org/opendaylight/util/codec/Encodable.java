/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

/**
 * A class that implements {@code Encodable} is declaring that its
 * internal state can be encoded as a String.
 * <p>
 * The general contract for an object implementing this interface is to:
 * <ol>
 * <li> implement the instance method {@link #toEncodedString toEncodedString}
 *      to create a string that embodies the state of the instance </li>
 * <li> implement a <em>static</em> method {@code valueOf(String)} which will
 *      return an instance re-constituted from the given encoded string </li>
 * </ol>
 * The {@code valueOf()} method should throw {@code NullPointerException} and
 * {@code IllegalArgumentException} as appropriate.
 * <p>
 * For {@code SomeEncodable} that implements {@code Encodable} the following
 * should hold true:
 * <pre>
 * SomeEncodable some = new SomeEncodable(...);
 * String blueprint = some.toEncodedString();
 * SomeEncodable copy = SomeEncodable.valueOf(blueprint);
 * assert copy.equals(some);
 * </pre>
 *
 * @author Simon Hunt
 */

public interface Encodable {

    /** Returns a string that is an encodement of this instance. 
     *
     * @return a string encodement of this instance
     */
    public String toEncodedString();
}
