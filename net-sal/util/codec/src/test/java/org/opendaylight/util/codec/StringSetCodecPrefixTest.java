/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

/**
 * This JUnit test class tests the StringSetCodec class, focusing
 * on the PREFIX encoding algorithm.
 *
 * @author Simon Hunt
 */
public class StringSetCodecPrefixTest extends AbstractStringSetCodecTest {

    private static final String[] ORIGINALS = {
        "Simple", "Simon", "Met", "A", "Pieman", "Going", "To", "The", "Fayre",
        "That", "Simmering", "Pie", "Looks", "Really", "Yummy", "Said", "Simeon",
        "Perhaps", "I", "Might", "Theoretically", "Consume", "Some", "Portion", "Of",
        "This", "Perfectly", "Presented", "Delicious", "Dessert",
    };

    @Override
    protected String[] getOriginalsArray() {
        return ORIGINALS.clone();
    }

    @Override
    protected Algorithm getAlgorithm() {
        return Algorithm.PREFIX;
    }
}
