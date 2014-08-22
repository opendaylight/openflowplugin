/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.util.json.JSON;
import org.opendaylight.util.json.JsonFactory;
import org.opendaylight.util.json.JsonValidationException;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;

/**
 * Unit tests for {@link DataPathInfoCodec}.
 *
 * @author Liem Nguyen
 */
public class DataPathInfoCodecTest extends AbstractCodecTest {

    private static final String JSON_DP_13 = "v13/datapath";
    private static final String JSON_DP_DECODE_13 = "v13/datapath_decode";
    private static final String JSON_DP_10 = "v10/datapath";
    private static final String JSON_DP_LIST_13 = "v13/datapaths";

    @BeforeClass
    public static void oneTimeInit() {
        JsonFactory factory = new OfJsonFactory();
        JSON.registerFactory(factory);
    }

    @Test
    public void testDataPathInfo13() {
        DataPathInfo dp = new MockDataPathInfo(V_1_3);
        String exp = getJsonContents(JSON_DP_13);
        String act = JSON.toJson(dp, true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(act));

        // Validate json via schema
        validate(act, DataPathInfoCodec.ROOT);
    }

    @Test
    public void testDataPathInfo10() {
        DataPathInfo dp = new MockDataPathInfo(V_1_0);
        String exp = getJsonContents(JSON_DP_10);
        String act = JSON.toJson(dp, true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(act));

        // Validate json via schema
        validate(act, DataPathInfoCodec.ROOT);
    }

    @Test
    public void testNullDpList() {
        assertEquals(AM_NEQ, normalizeEOL("{\"datapaths\":[]}"),
                normalizeEOL(JSON.toJsonList(null, DataPathInfo.class)));
    }

    @Test
    public void testDataPathInfoList13() {
        DataPathInfo dp = new MockDataPathInfo(V_1_3);
        String exp = getJsonContents(JSON_DP_LIST_13);
        String act = JSON.toJsonList(Arrays.asList(dp), DataPathInfo.class,
                true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(act));

        // Validate json via schema
        validate(act, DataPathInfoCodec.ROOTS);
    }

    @Ignore("put back when schema valiation is enabled")
    @Test(expected = JsonValidationException.class)
    public void testBadDataPathInfoList13() {
        // dpid is required
        String badData = getJsonContents(JSON_DP_LIST_13)
                .replaceAll("\"dpid\"", "\"blah\"");
        validate(badData, DataPathInfoCodec.ROOTS);
    }

    @Test
    public void testDataPathInfo_decode13() {
        String exp = getJsonContents(JSON_DP_DECODE_13);
        DataPathInfo dp = JSON.fromJson(exp, DataPathInfo.class);
        String actual = JSON.toJson(dp, true);
        assertEquals(normalizeEOL(exp), normalizeEOL(actual));
        validate(actual, DataPathInfoCodec.ROOT);
    }
}
