/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

import org.junit.Test;

import java.util.*;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This JUnit test class tests the CodecFactory class.
 *
 * @author Simon Hunt
 */
public class CodecFactoryTest {

    private static final String FRED = "Fred";
    private static final String WILMA = "Wilma";
    private static final String PEBBLES = "Pebbles";
    private static final String PEARL = "Pearl";

    private static final String BARNEY = "Barney";
    private static final String BETTY = "Betty";
    private static final String BAMM_BAMM = "Bamm-Bamm";

    private static final Set<String> FLINTSTONES =
            new TreeSet<String>(Arrays.asList(FRED, WILMA, PEBBLES, PEARL));

    private static final Set<String> RUBBLES =
            new TreeSet<String>(Arrays.asList(BARNEY, BETTY, BAMM_BAMM));

    private StringSetCodec ssc;
    private EntropicStringSetCodec essc;

    private StringCodec codec;
    private EntropicStringCodec entropicCodec;

    @Test
    public void stringSetCodec() {
        print(EOL + "stringSetCodec()");
        ssc = CodecFactory.createStringSetCodec(FLINTSTONES, Algorithm.PREFIX);
        print(ssc.toDebugString());
        assertEquals(AM_UXS, FLINTSTONES.size(), ssc.getToEncMapRef().size());

        codec = ssc; // use interface reference

        String fredEnc = codec.encode(FRED);
        String fredOrig = codec.decode(fredEnc);
        assertEquals(AM_NEQ, FRED, fredOrig);
    }

    @Test (expected = IllegalArgumentException.class)
    public void stringSetCodecUnknownOriginal() {
        ssc = CodecFactory.createStringSetCodec(FLINTSTONES, Algorithm.PREFIX);
        codec = ssc; // use interface reference
        codec.encode(BARNEY); // Barney isn't a Flintstone
    }


    @Test
    public void entropicStringSetCodec() {
        print(EOL + "entropicStringSetCodec()");
        essc = CodecFactory.createEntropicStringSetCodec(FLINTSTONES,
                                                         Algorithm.PREFIX);
        print(essc.toDebugString());
        assertEquals(AM_UXS, FLINTSTONES.size(), essc.getToEncMapRef().size());

        entropicCodec = essc; // use interface reference

        String wilmaEnc = entropicCodec.encode(WILMA);
        String wilmaOrig = entropicCodec.decode(wilmaEnc);
        assertEquals(AM_NEQ, WILMA, wilmaOrig);

        // this time we can add the Rubbles...
        entropicCodec.add(BARNEY);
        print(essc.toDebugString());
        assertEquals(AM_UXS, FLINTSTONES.size() + 1, essc.getToEncMapRef().size());

        entropicCodec.addAll(RUBBLES);
        print(essc.toDebugString());
        assertEquals(AM_UXS, FLINTSTONES.size() + RUBBLES.size(), essc.getToEncMapRef().size());
    }

    @Test
    public void emptyEntropicStringSetCodec() {
        print(EOL + "emptyEntropicStringSetCodec()");
        essc = CodecFactory.createEntropicStringSetCodec(null, Algorithm.PREFIX);
        print(essc.toDebugString());
        assertEquals(AM_UXS, 0, essc.getToEncMapRef().size());

        entropicCodec = essc; // use interface reference

        entropicCodec.addAll(RUBBLES);
        print(essc.toDebugString());
        assertEquals(AM_UXS, RUBBLES.size(), essc.getToEncMapRef().size());
    }

    @Test(expected = NullPointerException.class)
    public void stringSetCodecNullOriginals() {
        CodecFactory.createStringSetCodec(null, Algorithm.PREFIX);
    }

    @Test(expected = IllegalArgumentException.class)
    public void stringSetCodecEmptyOriginals() {
        CodecFactory.createStringSetCodec(Collections.<String>emptySet(),
                                          Algorithm.PREFIX);
    }

    private static final String[] FOO_ARRAY = {
            "foo", "bar", "baz", "xyyzy",
    };
    private static final Set<String> FOO =
            new HashSet<String>(Arrays.asList(FOO_ARRAY));

    @Test
    public void stringSetCodecNullAlgorithm() {
        print(EOL + "stringSetCodecNullAlgorithm()");
        StringSetCodec c = CodecFactory.createStringSetCodec(FOO, null);
        print(c.toDebugString());
        assertEquals(AM_UXS, FOO.size(), c.size());
        assertEquals("wrong algorithm", Algorithm.PREFIX, c.getAlgorithm());
    }

    @Test
    public void entropicStringSetCodecNullAlgorithm() {
        print(EOL + "entropicStringSetCodecNullAlgorithm()");
        EntropicStringSetCodec c = CodecFactory.createEntropicStringSetCodec(FOO, null);
        print(c.toDebugString());
        assertEquals(AM_UXS, FOO.size(), c.size());
        assertEquals("wrong algorithm", Algorithm.PREFIX, c.getAlgorithm());
    }

    @Test
    public void getGeneralCodec() {
        print(EOL + "getGeneralCodec");
        StringSetCodec c = (StringSetCodec) CodecFactory.createGeneralCodec(FOO);
        print(c.toDebugString());
        assertEquals("wrong algorithm", Algorithm.PREFIX, c.getAlgorithm());
    }

    private static enum Color { RED, YELLOW, GREEN, BLUE, BLACK, WHITE }

    @Test
    public void getCodecForEnum() {
        print(EOL + "getCodecForEnum()");
        StringCodec c = CodecFactory.createCodecForEnumNames(Color.class);
        print(((StringSetCodec)c).toDebugString());
        assertEquals("wrong mapping", "RE", c.encode(Color.RED.name()));
        assertEquals("wrong mapping", "BL", c.encode(Color.BLUE.name()));
        assertEquals("wrong mapping", "BL1", c.encode(Color.BLACK.name()));
    }

    @Test
    public void getCodecForClassNames() {
        print(EOL + "getCodecForClassNames()");
        StringCodec c = CodecFactory.createEntropicCodecForClassNames();
        assertTrue("wrong class of codec", c instanceof ClassNameCodec);
    }

    private static enum Bug { LADY, DEFECT, ABOO }

    @Test
    public void getEnumCodec() {
        print(EOL + "getEnumCodec()");
        EnumCodec<Bug> codec = CodecFactory.createEnumCodec(Bug.class);
        print(codec.toDebugString());
    }
}
