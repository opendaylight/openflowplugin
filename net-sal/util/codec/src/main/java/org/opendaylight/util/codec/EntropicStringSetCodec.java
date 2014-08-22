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
 * This class is a specialization of {@link StringSetCodec} that allows
 * mappings to be added to the codec after it has been constructed.
 *
 * @see CodecFactory
 * @author Simon Hunt
 */
public class EntropicStringSetCodec extends StringSetCodec
                                    implements EntropicStringCodec {


    /** Constructs an implementation using the given set of strings,
     * and employing the specified encoding algorithm.
     *
     * @param originals the set of strings to create a mapping for
     * @param algorithm the encoding algorithm to use
     */
    // package-private
    EntropicStringSetCodec(Set<String> originals, Algorithm algorithm) {
        super(originals, algorithm);
    }

    @Override
    public void add(String original) {
        if (original == null)
            throw new NullPointerException("cannot map null");
        addMapping(original);
    }

    @Override
    public void addAll(Set<String> originals) {
        if (originals == null)
            throw new NullPointerException("parameter cannot be null");

        for (String s: originals)
            if (s==null)
                throw new NullPointerException("cannot map null");

        for (String s: originals)
            addMapping(s);
    }
}
