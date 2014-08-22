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

import java.util.*;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This class provides the basis for testing the StringSetCodec class.
 * Concrete subclasses focus on a particular encoding algorithm.
 *
 * @author Simon Hunt
 */
public abstract class AbstractStringSetCodecTest {

    private static final String UNKNOWN_STR = "Unknown";
    private static final String UNKNOWN_ENC = "Un9";

    protected StringSetCodec ssc;
    protected Map<String,String> toEnc;
    protected Map<String,String> fromEnc;
    protected String cuMagic;
    private String[] origs;

    /** Concrete subclass must provide an array of original strings.
     *
     * @return the array of original strings
     */
    protected abstract String[] getOriginalsArray();

    /** Concrete subclass must provide the algorithm they want to test.
     *
     * @return the encoding algorithm
     */
    protected abstract Algorithm getAlgorithm();


    @Before
    public void setUp() {
        origs = getOriginalsArray();
        Set<String> originals = new HashSet<String>();
        originals.addAll(Arrays.asList(origs));
        // make sure the originals contains no duplicates
        assertTrue("Originals array contains duplicates",
                        originals.size() == origs.length);
        ssc = new StringSetCodec(originals, getAlgorithm());
        toEnc = ssc.getToEncMapRef();
        fromEnc = ssc.getFromEncMapRef();
        cuMagic = CodecUtils.getMagic();
    }


    @Test
    public void basic() {
        print(EOL + "basic()");
        print(" Algorithm: " + getAlgorithm().name() + " code=" + getAlgorithm());
        print(" Mappings:");
        for (String s: origs)
            print("   " + s + " <==> " + ssc.encode(s));
    }

    @Test (expected = IllegalArgumentException.class)
    public void unknownOriginal() {
        assertFalse(AM_HUH, toEnc.keySet().contains(UNKNOWN_STR));
        ssc.encode(UNKNOWN_STR);
    }

    @Test (expected = IllegalArgumentException.class)
    public void unknownEncodement() {
        assertFalse(AM_HUH, fromEnc.keySet().contains(UNKNOWN_ENC));
        ssc.decode(UNKNOWN_ENC);
    }

    @Test
    public void testAllEncodings() {
        String[] encodings = new String[origs.length];

        // get and save the encodings for each string
        for (int i=0,n=origs.length; i<n; i++)
            encodings[i] = ssc.encode(origs[i]);

        // verify that we get back the originals when we decode
        for (int i=0,n=origs.length; i<n; i++)
            assertEquals(AM_VMM, origs[i], ssc.decode(encodings[i]));
    }


    @Test
    public void generateBluePrint() {
        print(EOL + "generateBluePrint()");
        String blueprint = ssc.toEncodedString();
        print(blueprint);
        StringSetCodec copy = StringSetCodec.valueOf(blueprint);
        verifyEqual(ssc, copy);
    }

    @Test (expected = NullPointerException.class)
    public void valueOfNull() {
        StringSetCodec.valueOf(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfBadMagic() {
        StringSetCodec.valueOf(cuMagic + ",7,MAGIC,p,2,21,Pe,Perhaps,1,");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfBadPreserve() {
        StringSetCodec.valueOf(cuMagic + ",7,ssc1,p,JAM,21,Pe,Perhaps,1,");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfBadBaseSize() {
        StringSetCodec.valueOf(cuMagic + ",7,ssc1,p,2,BSIZE,Pe,Perhaps,1,");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfInsufficientBaseData() {
        StringSetCodec.valueOf(cuMagic + ",7,ssc1,p,2,2,Pe,Perhaps,1,");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfBadResiduals() {
        StringSetCodec.valueOf(cuMagic + ",8,ssc1,p,2,1,Pe,Perhaps,1,Pe1,");
    }


    private static final String[] CAT = {
            "the", "cat", "in", "the", "hat",
    };
    private static final String[] GATO = {
            "the", "cat", "in", "the", "sombrero",
    };
    private static final int CAT_COUNT = 4; // unique

    @Test
    public void equality() {
        print(EOL + "equality()");
        Set<String> catSet = new TreeSet<String>(Arrays.asList(CAT));
        StringSetCodec catOne = new StringSetCodec(catSet, getAlgorithm());
        print(catOne.toDebugString());
        assertEquals(AM_UXS, CAT_COUNT, catOne.getToEncMapRef().size());

        StringSetCodec catTwo = new StringSetCodec(catSet, getAlgorithm());
        verifyEqual(catOne, catTwo);

        Set<String> gatoSet = new TreeSet<String>(Arrays.asList(GATO));
        StringSetCodec catThree = new StringSetCodec(gatoSet, getAlgorithm());
        print(catThree.toDebugString());
        assertEquals(AM_UXS, CAT_COUNT, catThree.getToEncMapRef().size());
        verifyNotEqual(catTwo, catThree);
    }

    @Test (expected = NullPointerException.class)
    public void encodingNull() {
        ssc.encode(null);
    }

    @Test (expected = NullPointerException.class)
    public void decodingNull() {
        ssc.decode(null);
    }

    @Test
    public void addMapping() {
        print(EOL + "addMapping()");
        Set<String> catSet = new TreeSet<String>(Arrays.asList(CAT));
        StringSetCodec americanCat = new StringSetCodec(catSet, getAlgorithm());
        assertEquals(AM_UXS, CAT_COUNT, americanCat.getToEncMapRef().size());

        Set<String> gatoSet = new TreeSet<String>(Arrays.asList(GATO));
        StringSetCodec mexicanCat = new StringSetCodec(gatoSet, getAlgorithm());
        assertEquals(AM_UXS, CAT_COUNT, mexicanCat.getToEncMapRef().size());

        verifyNotEqual(americanCat, mexicanCat);
        americanCat.addMapping("sombrero");
        mexicanCat.addMapping("hat");
        print(mexicanCat.toDebugString());
        verifyEqual(americanCat, mexicanCat);
    }

    @Test (expected = NullPointerException.class)
    public void addMappingNull() {
        Set<String> catSet = new TreeSet<String>(Arrays.asList(CAT));
        StringSetCodec americanCat = new StringSetCodec(catSet, getAlgorithm());
        americanCat.addMapping(null);
    }

    @Test
    public void stringRep() {
        print(EOL + "stringRep()");
        print("toString()---");
        print(ssc);
        print("toDebugString()---");
        print(ssc.toDebugString());
    }
}
