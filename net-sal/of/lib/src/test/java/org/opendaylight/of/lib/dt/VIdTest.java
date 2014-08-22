/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link VId}.
 *
 * @author Simon Hunt
 */
public class VIdTest extends U16IdTest {

    private VId vid;
    private VId vidAlt;

    @Test
    public void min() {
        vid = VId.valueOf(ID_MIN);
        assertEquals(AM_NEQ, ID_MIN, vid.toInt());
        assertEquals(AM_NEQ, ID_MIN_STR_DEC, vid.toString());
        vidAlt = VId.valueOf(ID_MIN_STR_DEC);
        assertEquals(AM_NSR, vid, vidAlt);
        vidAlt = VId.valueOf(ID_MIN_STR_HEX);
        assertEquals(AM_NSR, vid, vidAlt);
    }

    @Test
    public void low() {
        vid = VId.valueOf(ID_LOW);
        assertEquals(AM_NEQ, ID_LOW, vid.toInt());
        assertEquals(AM_NEQ, ID_LOW_STR_DEC, vid.toString());
        vidAlt = VId.valueOf(ID_LOW_STR_DEC);
        assertEquals(AM_NSR, vid, vidAlt);
        vidAlt = VId.valueOf(ID_LOW_STR_HEX);
        assertEquals(AM_NSR, vid, vidAlt);
    }

    @Test
    public void high() {
        vid = VId.valueOf(ID_HIGH);
        assertEquals(AM_NEQ, ID_HIGH, vid.toInt());
        assertEquals(AM_NEQ, ID_HIGH_STR_DEC, vid.toString());
        vidAlt = VId.valueOf(ID_HIGH_STR_DEC);
        assertEquals(AM_NSR, vid, vidAlt);
        vidAlt = VId.valueOf(ID_HIGH_STR_HEX);
        assertEquals(AM_NSR, vid, vidAlt);
    }

    @Test
    public void max() {
        vid = VId.valueOf(ID_MAX);
        assertEquals(AM_NEQ, ID_MAX, vid.toInt());
        assertEquals(AM_NEQ, ID_MAX_STR_DEC, vid.toString());
        vidAlt = VId.valueOf(ID_MAX_STR_DEC);
        assertEquals(AM_NSR, vid, vidAlt);
        vidAlt = VId.valueOf(ID_MAX_STR_HEX);
        assertEquals(AM_NSR, vid, vidAlt);
    }

    @Test(expected = NullPointerException.class)
    public void nullStringBid() {
        vid = VId.valueOf((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void nullByteArrayBid() {
        vid = VId.valueOf((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shortByteArrayBid() {
        vid = VId.valueOf(new byte[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void longByteArrayBid() {
        vid = VId.valueOf(new byte[3]);
    }

    @Test
    public void fromBytesHigh() {
        vid = VId.valueOf(ID_HIGH_BYTES);
        assertEquals(AM_NEQ, ID_HIGH, vid.toInt());
    }

    @Test
    public void toBytesHigh() {
        byte[] bytes = VId.valueOf(ID_HIGH).toByteArray();
        assertArrayEquals(AM_NEQ, ID_HIGH_BYTES, bytes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void under() {
        vid = VId.valueOf(ID_UNDER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void over() {
        vid = VId.valueOf(ID_OVER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overStr() {
        vid = VId.valueOf(ID_OVER_STR_DEC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fooeyErrorMsg() {
        vid = VId.valueOf(FOOEY);
    }

    @Test
    public void compare() {
        print(EOL + "compare()");
        int count = UNSORTED.length;
        VId[] gids = new VId[count];
        for (int i=0; i<count; i++)
            gids[i] = VId.valueOf(UNSORTED[i]);
        print("Unsorted...");
        print(Arrays.toString(gids));
        Arrays.sort(gids);
        print("Sorted...");
        print(Arrays.toString(gids));
        for (int i=0; i<count; i++)
            assertEquals(AM_NEQ, SORTED[i], gids[i].toInt());
    }

    private static final VId[] UNSORTED_VIDS = {
            vid(42),
            vid(13),
            VId.NONE,
            vid(3),
            VId.PRESENT,
            vid(25),
    };

    private static final VId[] SORTED_VIDS = {
            VId.NONE,
            VId.PRESENT,
            vid(3),
            vid(13),
            vid(25),
            vid(42),
    };

    @Test
    public void vidNone() {
        print(EOL + "vidNone()");
        VId v = VId.NONE;
        print(v);
        assertEquals(AM_NEQ, "NONE", v.toString());
    }

    @Test
    public void vidPresent() {
        print(EOL + "vidPresent()");
        VId v = VId.PRESENT;
        print(v);
        assertEquals(AM_NEQ, "PRESENT", v.toString());
    }

    @Test
    public void newCompare() {
        print(EOL + "compare()");
        int count = UNSORTED_VIDS.length;
        print("Unsorted...");
        print(Arrays.toString(UNSORTED_VIDS));
        VId[] sorted = UNSORTED_VIDS.clone();
        Arrays.sort(sorted);
        print("Sorted...");
        print(Arrays.toString(sorted));
        for (int i=0; i<count; i++)
            assertEquals(AM_NEQ, SORTED_VIDS[i], sorted[i]);
    }

    @Test
    public void vidNoneFromString() {
        print(EOL + "vidNoneFromString()");
        VId v = VId.valueOf("none");
        print(v);
        assertEquals(AM_NEQ, VId.NONE, v);
        v = VId.valueOf("NONE");
        print(v);
        assertEquals(AM_NEQ, VId.NONE, v);
    }

    @Test
    public void vidPresentFromString() {
        print(EOL + "vidPresentFromString()");
        VId v = VId.valueOf("present");
        print(v);
        assertEquals(AM_NEQ, VId.PRESENT, v);
        v = VId.valueOf("PRESENT");
        print(v);
        assertEquals(AM_NEQ, VId.PRESENT, v);
    }

    @Test
    public void vid42FromString() {
        print(EOL + "vid42FromString()");
        VId v = VId.valueOf("42");
        print(v);
        assertEquals(AM_NEQ, vid(42), v);
    }

    private static final String SOME_VAL = "32";
    
    @Test
    public void convenience() {
        assertEquals(AM_NEQ, VId.valueOf(SOME_VAL), VId.vid(SOME_VAL));
    }
}
