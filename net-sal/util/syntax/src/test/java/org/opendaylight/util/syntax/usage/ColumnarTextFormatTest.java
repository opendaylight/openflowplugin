/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.util.syntax.usage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.StringTokenizer;

import org.junit.Test;

public class ColumnarTextFormatTest {
    
    @SuppressWarnings("unused")
    private static void print(Object o) { 
        // System.out.println(o); 
    }

    private static final String FOO =
        "You know we are on a wrong track altogether. We must not think of " +
        "the things we could do with, but only of the things that we can't " +
        "do without."; 
        
    private void verify(String string, String firstLinePrefix, String prefix, 
                        int length) {
        StringTokenizer st = new StringTokenizer(string, "\n");
        String line = st.nextToken();
        assertTrue("line too long", line.length() <= length);
        assertTrue("incorrect prefix", line.startsWith(firstLinePrefix)); 
        while (st.hasMoreTokens()) {
            line = st.nextToken();
            assertTrue("line too long", line.length() <= length);
            assertTrue("incorrect prefix", line.startsWith(prefix)); 
            assertFalse("prefix too long", line.startsWith(prefix + " ")); 
        }
    }
    
    @Test
    public void testAlignBlock() {
        String foo = ColumnarTextFormat.alignBlock(FOO, 7);
        verify(foo, "       You know", "       ", 
               ColumnarTextFormat.DEFAULT_MAXIMUM_LINE_LENGTH);
        print(foo);

        foo = ColumnarTextFormat.alignBlock(FOO, 5, 40, "\n");
        verify(foo, "     You know", "     ", 40);
        print(foo);
    }

    @Test
    public void testAlignTwoColumns() {
        String foo = ColumnarTextFormat.alignTwoColumns(FOO, 2, 5);
        print(foo);
        verify(foo, "  You     know", "          ",  
               ColumnarTextFormat.DEFAULT_MAXIMUM_LINE_LENGTH);

        foo = ColumnarTextFormat.alignTwoColumns(FOO, 3, 7, 62);
        print(foo);
        verify(foo, "   You >>>>> know", "             ",  
               ColumnarTextFormat.DEFAULT_MAXIMUM_LINE_LENGTH);

        foo = ColumnarTextFormat.alignTwoColumns(FOO, 4, 4, 60, 100);
        print(foo);
        verify(foo, "    You << know", "           ",  100);
    }    
    
}
