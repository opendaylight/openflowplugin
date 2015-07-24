/*
 *
 *  * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *
 */

package org.opendaylight.openflowplugin.impl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlowUtilTest {

    private static final short DUMMY_TABLE_ID = 1;
    public static final Pattern INDEX_PATTERN = Pattern.compile("^#UF\\$TABLE\\*1-([0-9]+)$");

    @Test
    public void createAlienFlowIdTest() {
        final String alienFlowId1 = FlowUtil.createAlienFlowId(DUMMY_TABLE_ID).getValue();
        final Integer index1 = parseIndex(alienFlowId1);
        final String alienFlowId2 = FlowUtil.createAlienFlowId(DUMMY_TABLE_ID).getValue();
        final Integer index2 = parseIndex(alienFlowId2);

        assertNotNull("index1 parsing failed: "+alienFlowId1, index1);
        assertNotNull("index2 parsing failed: "+alienFlowId2, index2);
        assertTrue(index1 < index2);
    }

    private static Integer parseIndex(String alienFlowIdValue) {
        final Matcher mach = INDEX_PATTERN.matcher(alienFlowIdValue);
        if (mach.find()) {
            return Integer.valueOf(mach.group(1));
        }
        return null;
    }

}
