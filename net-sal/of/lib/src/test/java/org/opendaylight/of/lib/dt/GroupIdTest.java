/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for GroupId.
 *
 * @author Simon Hunt
 */
public class GroupIdTest extends U32IdTest {

    // slight alteration of the notion of ANY, ALL, and MAX
    public static final long ID_ANY = 4294967295L;
    public static final String ID_ANY_STR_DEC = "4294967295";
    public static final String ID_ANY_STR_HEX = "0xffffffff";
    public static final String ID_ANY_STR_HEX_PLUS = "0xffffffff(ANY)";

    public static final long ID_ALL = 4294967292L;
    public static final String ID_ALL_STR_DEC = "4294967292";
    public static final String ID_ALL_STR_HEX = "0xfffffffc";
    public static final String ID_ALL_STR_HEX_PLUS = "0xfffffffc(ALL)";

    @SuppressWarnings("hiding")
    public static final long ID_MAX = 4294967040L;
    @SuppressWarnings("hiding")
    public static final String ID_MAX_STR_DEC = "4294967040";
    @SuppressWarnings("hiding")
    public static final String ID_MAX_STR_HEX = "0xffffff00";
    public static final String ID_MAX_STR_HEX_PLUS = "0xffffff00(MAX)";


    private GroupId gid;
    private GroupId gidAlt;

    @Test
    public void min() {
        gid = GroupId.valueOf(ID_MIN);
        assertEquals(AM_NEQ, ID_MIN, gid.toLong());
        assertEquals(AM_NEQ, ID_MIN_STR_HEX_PLUS, gid.toString());
        gidAlt = GroupId.valueOf(ID_MIN_STR_DEC);
        assertSame(AM_NSR, gid, gidAlt);
        gidAlt = GroupId.valueOf(ID_MIN_STR_HEX);
        assertSame(AM_NSR, gid, gidAlt);
    }

    @Test
    public void low() {
        gid = GroupId.valueOf(ID_LOW);
        assertEquals(AM_NEQ, ID_LOW, gid.toLong());
        assertEquals(AM_NEQ, ID_LOW_STR_HEX_PLUS, gid.toString());
        gidAlt = GroupId.valueOf(ID_LOW_STR_DEC);
        assertSame(AM_NSR, gid, gidAlt);
        gidAlt = GroupId.valueOf(ID_LOW_STR_HEX);
        assertSame(AM_NSR, gid, gidAlt);
    }

    @Test
    public void high() {
        gid = GroupId.valueOf(ID_HIGH);
        assertEquals(AM_NEQ, ID_HIGH, gid.toLong());
        assertEquals(AM_NEQ, ID_HIGH_STR_HEX_PLUS, gid.toString());
        gidAlt = GroupId.valueOf(ID_HIGH_STR_DEC);
        assertSame(AM_NSR, gid, gidAlt);
        gidAlt = GroupId.valueOf(ID_HIGH_STR_HEX);
        assertSame(AM_NSR, gid, gidAlt);
    }

    @Test
    public void max() {
        gid = GroupId.valueOf(ID_MAX);
        assertEquals(AM_NEQ, ID_MAX, gid.toLong());
        assertEquals(AM_NEQ, ID_MAX_STR_HEX_PLUS, gid.toString());
        gidAlt = GroupId.valueOf(ID_MAX_STR_DEC);
        assertSame(AM_NSR, gid, gidAlt);
        gidAlt = GroupId.valueOf(ID_MAX_STR_HEX);
        assertSame(AM_NSR, gid, gidAlt);
    }

    @Test
    public void all() {
        gid = GroupId.valueOf(ID_ALL);
        assertEquals(AM_NEQ, ID_ALL, gid.toLong());
        assertEquals(AM_NEQ, ID_ALL_STR_HEX_PLUS, gid.toString());
        gidAlt = GroupId.valueOf(ID_ALL_STR_DEC);
        assertSame(AM_NSR, gid, gidAlt);
        gidAlt = GroupId.valueOf(ID_ALL_STR_HEX);
        assertSame(AM_NSR, gid, gidAlt);
    }

    @Test
    public void any() {
        gid = GroupId.valueOf(ID_ANY);
        assertEquals(AM_NEQ, ID_ANY, gid.toLong());
        assertEquals(AM_NEQ, ID_ANY_STR_HEX_PLUS, gid.toString());
        gidAlt = GroupId.valueOf(ID_ANY_STR_DEC);
        assertSame(AM_NSR, gid, gidAlt);
        gidAlt = GroupId.valueOf(ID_ANY_STR_HEX);
        assertSame(AM_NSR, gid, gidAlt);
    }

    @Test(expected = NullPointerException.class)
    public void nullStringId() {
        gid = GroupId.valueOf((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void nullByteArrayId() {
        gid = GroupId.valueOf((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shortByteArrayId() {
        gid = GroupId.valueOf(new byte[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void longByteArrayId() {
        gid = GroupId.valueOf(new byte[5]);
    }

    @Test
    public void fromBytesHigh() {
        gid = GroupId.valueOf(ID_HIGH_BYTES);
        assertEquals(AM_NEQ, ID_HIGH, gid.toLong());
    }

    @Test
    public void toBytesHigh() {
        byte[] bytes = GroupId.valueOf(ID_HIGH).toByteArray();
        assertArrayEquals(AM_NEQ, ID_HIGH_BYTES, bytes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void under() {
        gid = GroupId.valueOf(ID_UNDER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void over() {
        gid = GroupId.valueOf(ID_OVER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overStr() {
        gid = GroupId.valueOf(ID_OVER_STR_DEC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fooeyErrorMsg() {
        gid = GroupId.valueOf(FOOEY);
    }

    @Test
    public void compare() {
        print(EOL + "compare()");
        int count = UNSORTED.length;
        GroupId[] gids = new GroupId[count];
        for (int i=0; i<count; i++) {
            gids[i] = GroupId.valueOf(UNSORTED[i]);
        }
        print("Unsorted...");
        print(Arrays.toString(gids));
        Arrays.sort(gids);
        print("Sorted...");
        print(Arrays.toString(gids));
        for (int i=0; i<count; i++) {
            assertEquals(AM_NEQ, SORTED[i], gids[i].toLong());
        }
    }
    
    private static final String SOME_VAL = "5";
    
    @Test
    public void convenience() {
        print(EOL + "convenience()");
        assertEquals(AM_NEQ, GroupId.valueOf(SOME_VAL), GroupId.gid(SOME_VAL));
    }

}
