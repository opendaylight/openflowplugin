/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

import org.junit.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * This JUnit test class tests the StringSetCodec class, focusing
 * on the PREFIX encoding algorithm.
 *
 * @author Simon Hunt
 */
public class StringSetCodecClassNamesTest extends AbstractStringSetCodecTest {

    private static final String[] ORIGINALS = {
            "org.opendaylight.util.Codec",
            "org.opendaylight.util.StringCodec",
            "org.opendaylight.util.EntropicStringCodec",
            "org.opendaylight.util.StringSetCodec",
            "org.opendaylight.util.EntropicStringSetCodec",
    };

    @Override
    protected String[] getOriginalsArray() {
        return ORIGINALS.clone();
    }

    @Override
    protected Algorithm getAlgorithm() {
        return Algorithm.CLASS_NAMES;
    }


    // ============= the following tests are for the CLASS_NAME algorithm

    private static final String FOO = "org.opendaylight.util.foo.Foo";
    private static final String BAR = "org.opendaylight.util.foo.Bar";
    private static final String BAZ = "org.opendaylight.util.foo.Baz";
    private static final String CF = "org.opendaylight.util.codec.CodecFactory";
    private static final String CU = "org.opendaylight.util.codec.CodecUtils";
    private static final String SC = "org.opendaylight.util.codec.StringCodec";

    private static final String[] DOT_DELIMITED = { FOO, BAR, BAZ, CF, CU, SC };

    private static final String FOO_REV = "Foo.foo.util.opendaylight.org";
    private static final String BAR_REV = "Bar.foo.util.opendaylight.org";
    private static final String BAZ_REV = "Baz.foo.util.opendaylight.org";
    private static final String CF_REV = "CodecFactory.codec.util.opendaylight.org";
    private static final String ABC = "Abc";

    private void checkReverse(String orig, String exp) {
        String rev = StringSetCodec.reverse(orig);
        print(orig + " --[reverse]--> " + rev);
        assertEquals(AM_NEQ, exp, rev);
    }

    @Test
    public void reverseMethod() {
        print(EOL + "reverseMethod()");
        checkReverse(FOO, FOO_REV);
        checkReverse(BAR, BAR_REV);
        checkReverse(BAZ, BAZ_REV);
        checkReverse(CF, CF_REV);
        checkReverse(ABC, ABC);
    }

    private void checkBadReversal(String s) {
        try {
            StringSetCodec.reverse(s);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("EX> " + e);
        }
    }

    private static final String[] BAD_REVERSALS = {
            ".",
            "..",
            "X..",
    };

    @Test
    public void badReversals() {
        print(EOL + "badReversals()");
        for (String s: BAD_REVERSALS)
            checkBadReversal(s);
    }

    @Test
    public void classNameBasic() {
        print(EOL + "classNameBasic()");
        Set<String> ddSet = new TreeSet<String>(Arrays.asList(DOT_DELIMITED));
        ssc = new StringSetCodec(ddSet, Algorithm.CLASS_NAMES);
        print(ssc.toDebugString());
        assertEquals(AM_VMM, "Fo", ssc.encode(FOO));
        assertEquals(AM_VMM, "Ba", ssc.encode(BAR));
        assertEquals(AM_VMM, "Ba1", ssc.encode(BAZ));
        assertEquals(AM_VMM, "Co", ssc.encode(CF));
        assertEquals(AM_VMM, "Co1", ssc.encode(CU));
        assertEquals(AM_VMM, "St", ssc.encode(SC));
    }

}
