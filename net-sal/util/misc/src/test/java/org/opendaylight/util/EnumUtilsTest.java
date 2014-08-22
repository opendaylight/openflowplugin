/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opendaylight.util.junit.TestTools.*;

import java.util.*;

import org.junit.Test;

/**
 * Enumeration utility test.
 * 
 * @author Frank Wood
 * @author Simon Hunt
 */
public class EnumUtilsTest {

    public static enum State {
        UNKNOWN("unk"),
        NORMAL("normal"),
        NOT_SUPPORTED("n/a");
        
        private String encoding;
        
        private State(String encoding) {
            this.encoding = encoding;
        }

        @Override
        public String toString() {
            return encoding;
        }
    }
    
    public static enum IntEnum implements EnumUtils.Coded {
        ONES(111),
        TWOS(222),
        THREES(333);
        
        private int encoding;
        
        private IntEnum(int encoding) {
            this.encoding = encoding;
        }

        @Override
        public String toString() {
            return String.valueOf(encoding);
        }

        @Override
        public int getCode() {
            return encoding;
        }
    }

    @Test
    public void getEnum() {
        print(EOL + "getEnum()");
        assertEquals(AM_NEQ, "unk", State.UNKNOWN.toString());
        assertEquals(AM_NEQ, "normal", State.NORMAL.toString());
        assertEquals(AM_NEQ, "n/a", State.NOT_SUPPORTED.toString());
        
        assertEquals(AM_NEQ, State.UNKNOWN,
                     EnumUtils.getEnum(State.class, "unk"));
        assertEquals(AM_NEQ, State.NORMAL,
                     EnumUtils.getEnum(State.class, "normal"));
        assertEquals(AM_NEQ, State.NOT_SUPPORTED,
                     EnumUtils.getEnum(State.class, "n/a"));
        
        assertNull(AM_HUH, EnumUtils.getEnum(State.class, "bogus"));
        
        assertEquals(AM_NEQ, "111", IntEnum.ONES.toString());
        assertEquals(AM_NEQ, "222", IntEnum.TWOS.toString());
        assertEquals(AM_NEQ, "333", IntEnum.THREES.toString());
        
        assertEquals(AM_NEQ, IntEnum.ONES,
                     EnumUtils.getEnum(IntEnum.class, 111));
        assertEquals(AM_NEQ, IntEnum.TWOS,
                     EnumUtils.getEnum(IntEnum.class, 222));
        assertEquals(AM_NEQ, IntEnum.THREES,
                     EnumUtils.getEnum(IntEnum.class, 333));
        
        assertNull(AM_HUH, EnumUtils.getEnum(IntEnum.class, "bogus"));
        
        List<State> stateEnums = new ArrayList<State>();
        stateEnums.add(State.UNKNOWN);
        stateEnums.add(State.NOT_SUPPORTED);
        print(stateEnums);

        List<IntEnum> intEnums = new ArrayList<IntEnum>();
        intEnums.add(IntEnum.TWOS);
        print(intEnums);
    }
    
    @Test
    public void codeLookup() {
        print(EOL + "codeLookup()");
        Map<Integer, IntEnum> map = EnumUtils.createLookup(IntEnum.class);

        assertEquals(AM_NEQ, "111", map.get(111).toString());
        assertEquals(AM_NEQ, "222", map.get(222).toString());
        assertEquals(AM_NEQ, "333", map.get(333).toString());
        assertNull(AM_HUH, map.get(321));

        assertEquals(AM_NEQ, IntEnum.ONES, map.get(111));
        assertEquals(AM_NEQ, IntEnum.TWOS, map.get(222));
        assertEquals(AM_NEQ, IntEnum.THREES, map.get(333));
    }

    @Test
    public void enumsByName() {
        print(EOL + "enumsByName()");
        List<String> names = new ArrayList<String>();
        Set<State> constants = new HashSet<State>();
        for (State s: State.values()) {
            constants.add(s);
            names.add(s.name());
        }
        print(names);

        Set<State> copy = new HashSet<State>();
        for (String str: names) {
            copy.add(EnumUtils.getEnumByName(State.class, str));
        }
        print(copy);
        assertEquals(AM_NEQ, constants, copy);
    }

    @Test
    public void enumsByNameNoMatch() {
        print(EOL + "enumsByNameNoMatch()");
        assertNull(AM_HUH, EnumUtils.getEnumByName(State.class,
                                                   "DOES_NOT_EXIST"));
    }
    
    @Test
    public void enumsByCode() {
        print(EOL + "enumsByCode()");
        assertEquals(AM_NEQ, IntEnum.THREES,
                     EnumUtils.getEnum(IntEnum.class, 333, IntEnum.ONES));
        assertEquals(AM_NEQ, IntEnum.ONES,
                     EnumUtils.getEnum(IntEnum.class, 321, IntEnum.ONES));
        assertEquals(AM_NEQ, IntEnum.TWOS,
                     EnumUtils.getEnum(IntEnum.class, 321, IntEnum.TWOS));
        assertEquals(AM_NEQ, IntEnum.TWOS,
                     EnumUtils.getEnum(IntEnum.class, 222, IntEnum.THREES));
        
    }    
}
