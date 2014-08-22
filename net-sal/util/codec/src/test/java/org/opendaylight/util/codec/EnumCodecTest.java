/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

import org.junit.Before;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.*;
import static junit.framework.Assert.assertEquals;

/**
 * This class tests the EnumCodec class.
 *
 * @author Simon Hunt
 */
public class EnumCodecTest {

    private static enum Foo { BAR, FLY, XYYZY }
    private static String[] ENCODED = { "BA", "FL", "XY" };

    private EnumCodec<Foo> codec;

    @Before
    public void setUp() {
        codec = new EnumCodec<Foo>(Foo.class);
    }

    @Test
    public void basics() {
        print(EOL + "basics()");
        print(codec.toDebugString());
    }

    @Test
    public void testEncoding() {
        print(EOL + "testEncoding()");
        int i=0;
        for (Foo f: Foo.values()) {
            String enc = codec.encode(f);
            print(f + " -> " + enc);
            assertEquals("wrong encoding", ENCODED[i++], enc);
        }
    }

    @Test
    public void testDecoding() {
        print(EOL + "testDecoding()");
        Foo[] consts = Foo.values();
        int i=0;
        for (String enc: ENCODED) {
            Foo f = codec.decode(enc);
            print(enc + " -> " + f);
            assertEquals("wrong const", consts[i++], f);
        }
    }

    @Test(expected = NullPointerException.class)
    public void nullClass() {
        codec = new EnumCodec<Foo>(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownEncoding() {
        codec.decode("something else");
    }

    @Test
    public void size() {
        print(EOL + "size()");
        print("codec size is " + codec.size());
        assertEquals(AM_UXS, Foo.values().length, codec.size());
    }
}
