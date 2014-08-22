/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

import org.opendaylight.util.EnumUtils;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.TallyMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.opendaylight.util.junit.TestTools.*;
import static junit.framework.Assert.assertEquals;

/**
 * This class unit tests the ClassNameCodec class.
 *
 * @author Simon Hunt
 */
public class ClassNameCodecTest {

    private static final Class<?>[] CLASSES = {
            Boolean.class,
            Integer.class,
            StringUtils.class,
            CodecUtils.class,
            CodecFactory.class,
            Algorithm.class,
            Encodable.class,
            EnumUtils.class,
            TallyMap.class,
    };

    private static final String ENC_BOOLEAN = "Bo";
    private static final String ENC_INTEGER = "In";
    private static final String ENC_STRING_UTILS = "St";
    private static final String ENC_CODEC_UTILS = "Co";
    private static final String ENC_CODEC_FACTORY = "Co1";
    private static final String ENC_ALGORITHM = "Al";
    private static final String ENC_ENCODABLE = "En";
    private static final String ENC_ENUM_UTILS = "En1";
    private static final String ENC_TALLY_MAP = "Ta";

    private static final String[] ENCODED = {
            ENC_BOOLEAN,
            ENC_INTEGER,
            ENC_STRING_UTILS,
            ENC_CODEC_UTILS,
            ENC_CODEC_FACTORY,
            ENC_ALGORITHM,
            ENC_ENCODABLE,
            ENC_ENUM_UTILS,
            ENC_TALLY_MAP,
    };

    private ClassNameCodec codec;

    @Before
    public void setUp() {
        codec = new ClassNameCodec();
        for (Class<?> c: CLASSES)
            codec.add(c);
    }

    @Test
    public void basics() {
        print(EOL + "basics()");
        print(codec.toDebugString());
    }

    @Test
    public void checkExpectedEncodings() {
        print(EOL + "checkExpectedEncodings()");
        for (int i=0,n=CLASSES.length; i<n; i++) {
            String actual = codec.encode(CLASSES[i]);
            String expected = ENCODED[i];
            assertEquals(AM_NEQ, expected, actual);
        }
    }

    @Test
    public void checkExpectedDecodings() {
        print(EOL + "checkExpectedDecodings()");
        for (int i=0,n=CLASSES.length; i<n; i++) {
            String actual = codec.decode(ENCODED[i]);
            String expected = CLASSES[i].getName();
            assertEquals(AM_NEQ, expected, actual);
        }
    }

    private void checkInstance(String exp, Object o) {
        String act = codec.encode(o);
        print("  " + o.toString() + " --> " + act);
        assertEquals(AM_NEQ, exp, act);
    }

    @Test
    public void someInstances() {
        print(EOL + "someInstances()");
        checkInstance(ENC_BOOLEAN, true);
        checkInstance(ENC_INTEGER, 123);
        checkInstance(ENC_ALGORITHM, Algorithm.PREFIX);
        checkInstance(ENC_TALLY_MAP, new TallyMap<Algorithm>());
    }

    @Test
    public void addAClassAndEncode() {
        print(EOL + "addAClassAndEncode()");
        Map<String, Integer> map = new TreeMap<String, Integer>();
        codec.add(map);
        print(codec.toDebugString());
        String e = codec.encode(map);
        print("TreeMap encoded as: " + e);
        assertEquals(AM_NEQ, "Tr", e);
    }

    @Test(expected = NullPointerException.class)
    public void encodeNullClass() {
        codec.encode((Class<?>) null);
    }

    @Test(expected = NullPointerException.class)
    public void encodeNullObject() {
        codec.encode((Object) null);
    }

}
