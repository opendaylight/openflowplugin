/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

/**
 * This JUnit test class tests the EntropicStringSetCodec class, focusing
 * on the PREFIX encoding algorithm.
 *
 * @author Simon Hunt
 */
public class EntropicStringSetCodecPrefixTest extends
        AbstractEntropicStringSetCodecTest {

    @Override
    protected Algorithm getAlgorithm() {
        return Algorithm.PREFIX;
    }
}
