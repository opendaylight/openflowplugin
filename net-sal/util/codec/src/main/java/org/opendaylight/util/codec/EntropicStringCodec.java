/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

import java.util.Set;

/**
 * This interface extends {@link StringCodec}, defining methods for adding new
 * string encodings after construction.
 *
 * @author Simon Hunt
 */
public interface EntropicStringCodec extends StringCodec {

    /** This method adds another original string to the codec.
     * The implementation will assign a unique encoded form for the string
     * and add it to its internal map.
     *
     * @param original an original (unencoded) string
     * @throws NullPointerException if the parameter is null
     */
    public void add(String original);

    /** This method adds more original (unencoded) strings to the codec.
     * The implementation will assign a unique encoded form for each string
     * and add them to its internal map.
     *
     * @param originals an original (unencoded) string
     * @throws NullPointerException if the parameter is null, or any element
     *          of the set is null
     */
    public void addAll(Set<String> originals);
}
