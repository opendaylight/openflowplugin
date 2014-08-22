/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertEquals;

/**
 * This abstract JUnit test class tests the EntropicStringSetCodec class.
 * Concrete subclasses should select the algorithm they are testing.
 *
 * @author Simon Hunt
 */
public abstract class AbstractEntropicStringSetCodecTest {

    private static final String HAMLET = "dane.Hamlet";
    private static final String ROSE = "friend.Rosencrantz";
    private static final String GUILD = "friend.Guildenstern";

    private EntropicStringSetCodec codec;
    private Algorithm algo;

    /** Concrete subclass should return the algo to test.
     *
     * @return the algo to test
     */
    protected abstract Algorithm getAlgorithm();

    @Before
    public void setUp() {
        algo = getAlgorithm();
        codec = new EntropicStringSetCodec(null, algo);
        assertEquals(AM_UXS, 0, codec.getToEncMapRef().size());
    }


    @Test
    public void basic() {
        print(EOL + "basic()");
        print(codec.toDebugString());
        print("adding hamlet");
        codec.add(HAMLET);
        print(codec.toDebugString());
        assertEquals(AM_UXS, 1, codec.getToEncMapRef().size());
    }

    @Test
    public void algorithm() {
        print(EOL + "algorithm()");
        assertEquals("wrong algorithm", algo, codec.getAlgorithm());
    }

    @Test
    public void nullIsOkay() {
        print(EOL + "nullIsOkay()");
        EntropicStringSetCodec nullParam =
                new EntropicStringSetCodec(null, algo);
        print(nullParam.toDebugString());
        assertEquals(AM_UXS, 0, nullParam.getToEncMapRef().size());
    }

    @Test (expected = NullPointerException.class)
    public void tryMappingNull() {
        print(EOL + "tryMappingNull()");
        print(codec);
        codec.add(null);
    }


    @Test
    public void duplicatesAreSilentlyIgnored() {
        print(EOL + "duplicatesAreSilentlyIgnored()");
        print(codec.toDebugString());
        codec.add(ROSE);
        print(codec.toDebugString());
        assertEquals(AM_UXS, 1, codec.getToEncMapRef().size());

        final String roseEnc = codec.encode(ROSE);
        print(ROSE + " is encoded as " + roseEnc);

        codec.add(GUILD);
        print(codec.toDebugString());
        assertEquals(AM_UXS, 2, codec.getToEncMapRef().size());

        final String guildEnc = codec.encode(GUILD);
        print(GUILD + " is encoded as " + guildEnc);

        codec.add(GUILD);
        print(codec.toDebugString());
        assertEquals(AM_UXS, 2, codec.getToEncMapRef().size());

        final String guildEnc2 = codec.encode(GUILD);
        assertEquals(AM_NEQ, guildEnc, guildEnc2);
    }

    @Test (expected = NullPointerException.class)
    public void addAllNull() {
        codec.addAll(null);
    }

    @Test (expected = NullPointerException.class)
    public void addAllEmbeddedNull() {
        print(EOL + "addAllEmbeddedNull()");

        codec.add(HAMLET);
        print(codec.toDebugString());
        assertEquals(AM_UXS, 1, codec.getToEncMapRef().size());

        Set<String> containsNullElement =
                new HashSet<String>(Arrays.asList(GUILD, null, ROSE));
        print(containsNullElement);
        codec.addAll(containsNullElement);
    }

    @Test
    public void addAllSoCalledFriends() {
        print(EOL + "addAllSoCalledFriends()");

        codec.add(HAMLET);
        print(codec.toDebugString());
        assertEquals(AM_UXS, 1, codec.getToEncMapRef().size());

        Set<String> someFriends = new HashSet<String>(Arrays.asList(GUILD, ROSE));
        print(someFriends);
        codec.addAll(someFriends);
        print(codec.toDebugString());
        assertEquals(AM_UXS, 3, codec.getToEncMapRef().size());
    }

}
