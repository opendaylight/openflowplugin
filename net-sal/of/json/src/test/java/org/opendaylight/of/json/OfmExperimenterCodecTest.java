/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.MessageType;
import org.opendaylight.of.lib.msg.OfmExperimenter;
import org.opendaylight.of.lib.msg.OfmMutableExperimenter;
import org.opendaylight.util.json.JSON;
import org.opendaylight.util.json.JsonFactory;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.print;

public class OfmExperimenterCodecTest extends AbstractCodecTest {

    private static final JsonFactory factory = OfJsonFactory.instance();
    private static final OfmExperimenterCodec codec = (OfmExperimenterCodec) factory
        .codec(OfmExperimenter.class);

    private static final String JSON_PATH = "v10/ofmExperimenterMod";

    private static final short EXP_CMD = 0;
    private static final short PAD = 0;
    private static final int EXP_ID = 1;
    private static final int EXP_SUB_TYPE = 0;

    private static final int KBPS_FLAG = 1;
    private static final int SET_DROP_FLAG = 4;
    private static final int CAPABILITY_FLAG = KBPS_FLAG + SET_DROP_FLAG;

    private static final int EXP_BSIZE = 1000;

    private static final int EXP_DROP_RATE = 1500;
    private static final int EXP_MARK_RATE = 0;
    private static final int MINIMUMLENGTH = 28;

    private static String experimenterJs = null;

    @BeforeClass
    public static void beforeClass() {
        JsonFactory factory = new OfJsonFactory();
        JSON.registerFactory(factory);
        experimenterJs = getJsonContents(JSON_PATH);
    }

    /**
     * An end-to-end test of ofmMeterModCodec of a single meter using
     * JSON.fromJson and JSON.toJson.
     */
    @Test
    public void testOfmExperimenterMod() {

        OfmExperimenter experimenter = JSON.fromJson(experimenterJs,
                                                     OfmExperimenter.class);
        print(JSON.toJson(experimenter, true));
        String actual = JSON.toJson(experimenter, true);
        assertEquals(normalizeEOL(experimenterJs),
                     normalizeEOL(JSON.toJson(experimenter, true)));
        validate(actual, OfmExperimenterCodec.ROOT);
    }

    @Test
    public void decode() {
        String actual = getJsonContents(JSON_PATH);

        OfmExperimenter experimenter = codec.decode(actual);

        byte[] data = experimenter.getData();
        ByteBuffer bb = ByteBuffer.wrap(data, 0, MINIMUMLENGTH);

        assertEquals(AM_NEQ, EXP_SUB_TYPE, bb.getInt());
        assertEquals(AM_NEQ, EXP_ID, bb.getInt());
        assertEquals(AM_NEQ, CAPABILITY_FLAG, bb.getInt());
        assertEquals(AM_NEQ, EXP_DROP_RATE, bb.getInt());
        assertEquals(AM_NEQ, EXP_MARK_RATE, bb.getInt());
        assertEquals(AM_NEQ, EXP_BSIZE, bb.getInt());
        assertEquals(AM_NEQ, EXP_CMD, bb.getShort());
        assertEquals(AM_NEQ, PAD, bb.getShort());

    }

    @Test
    public void encode() {

        OfmMutableExperimenter expMod = (OfmMutableExperimenter)
                MessageFactory.create(V_1_0, MessageType.EXPERIMENTER);

        ByteBuffer bb = ByteBuffer.allocate(MINIMUMLENGTH);
        bb.putInt(EXP_SUB_TYPE);
        bb.putInt(EXP_ID);
        bb.putInt(CAPABILITY_FLAG);
        bb.putInt(EXP_DROP_RATE);
        bb.putInt(EXP_MARK_RATE);
        bb.putInt(EXP_BSIZE);
        bb.putShort(EXP_CMD);
        bb.putShort(PAD);

        expMod.data(bb.array());

        String exp = getJsonContents(JSON_PATH);
        String actual = codec.encode(expMod, true);

        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
        validate(actual, OfmExperimenterCodec.ROOT);
    }

}
