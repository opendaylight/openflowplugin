/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.Test;
import org.opendaylight.of.lib.instr.ActSetMplsTtl;
import org.opendaylight.of.lib.instr.Action;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.json.ActionCodecTestUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.util.StringUtils.getFileContents;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;

/**
 * Unit tests for {@link ActionCodec}.
 *
 * @author Shaila Shree
 */
public class ActionCodecTest {
    private static final ActionCodec codec =
            (ActionCodec) OfJsonFactory.instance().codec(Action.class);

    private static final String EXP_JSON_PATH = "org/opendaylight/of/json/";
    private static final String V10 = "v10";
    private static final String V13 = "v13";

    private static final String ACTION = "action.json";
    private static final String ACTIONS = "actions.json";

    private final ClassLoader cl = getClass().getClassLoader();

    private String getJson(String dir, String name) throws IOException {
        return getFileContents(EXP_JSON_PATH + dir + "/" + name, cl);
    }

    @Test
    public void encode() throws IOException {
        String exp = getJson(V13, ACTION);
        String actual = codec.encode(createTestAction(), true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
    }

    @Test
    public void decode() throws IOException {
        String actual =  getJson(V13, ACTION);
        verifyTestAction((ActSetMplsTtl) codec.decode(actual));
        validate(actual, ActionCodec.ROOT);
    }

    @Test
    public void encodeListV10() throws IOException {
        String exp =  getJson(V10, ACTIONS);
        String actual = codec.encodeList(createAllActions(V_1_0),true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
    }

    @Test
    public void decodeListV10() throws IOException {
        String actual =  getJson(V10, ACTIONS);
        verifyAllActions(V_1_0, codec.decodeList(actual));
        validate(actual, ActionCodec.ROOTS);
    }

    @Test
    public void encodeListV13() throws IOException {
        String exp = getJson(V13, ACTIONS);
        String actual =  codec.encodeList(createAllActions(V_1_3), true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
    }

    @Test
    public void decodeListV13() throws IOException {
        String actual = getJson(V13, ACTIONS);
        verifyAllActions(V_1_1, codec.decodeList(actual));
        validate(actual, ActionCodec.ROOTS);
    }
}
