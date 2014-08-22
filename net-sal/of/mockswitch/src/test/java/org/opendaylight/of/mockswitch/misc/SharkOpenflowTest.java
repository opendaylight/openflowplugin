/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch.misc;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.MessageLibTestUtils;
import org.opendaylight.util.junit.SlowTests;
import org.opendaylight.util.junit.TestLogger;
import org.slf4j.Logger;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Developing SharkOpenflow class.
 *
 * @author Simon Hunt
 */
@Category(SlowTests.class)
public class SharkOpenflowTest {

    private static final String ROOT_DIR = "src/test/resources/org/opendaylight/of/mockswitch/misc/";

    private static final String LAG_FILE = "LAG-5900-OFmsgs.pdml.xml";
    private static final String PACKET_6600 = "packet_6600.pdml.xml";
    private static final String CR_141001 = "cr141001.pdml.xml";
    private static final String CR_139677 = "cr139677-blocked-port-state-change.pdml.xml";

    private static final String SEN_GOOD = "sentinel-good-flow-mod.xml";
    private static final String SEN_BAD = "sentinel-bad-flow-mod.xml";

    private static final String RIJESH_18 = "18-len10202.xml";
    private static final String SHAILA_MARCH = "shaila-march.xml";
    private static final String JULIE_PRUNES = "julie-prunes.xml";

    private SharkOpenflow so;

    private String path(String file) {
        return ROOT_DIR + file;
    }

    private static final TestLogger tlog = new TestLogger();

    private static class MyLibUtils extends MessageLibTestUtils {
        @Override
        protected void restoreMessageParserLogger() {
            super.restoreMessageParserLogger();
        }

        @Override
        protected void setMessageParserLogger(Logger logger) {
            super.setMessageParserLogger(logger);
        }
    }

    private static final MyLibUtils myLibUtils = new MyLibUtils();

    @BeforeClass
    public static void classSetUp() {
        myLibUtils.setMessageParserLogger(tlog);
    }

    @AfterClass
    public static void classTearDown() {
        myLibUtils.restoreMessageParserLogger();
    }

    @Test
    public void lag5900() {
        print(EOL + "lag5900()");
        so = new SharkOpenflow(path(LAG_FILE));
        print(so);
    }

    @Test @Ignore("message parse fail (fragmented message)")
    public void packet6600() {
        print(EOL + "packet6600()");
        so = new SharkOpenflow(path(PACKET_6600));
        print(so);
    }

    @Test
    public void cr141001() {
        print(EOL + "cr141001()");
        so = new SharkOpenflow(path(CR_141001));
        print(so);
    }

    @Test
    public void cr139677() {
        print(EOL + "cr139677()");
        so = new SharkOpenflow(path(CR_139677));
        print(so);
    }

    @Test
    public void sentinel() {
        print(EOL + "sentinel()");
        print(EOL + "Good Packet Capture");
        print(new SharkOpenflow(path(SEN_GOOD)));
        print(EOL + "Bad Packet Capture");
        print(new SharkOpenflow(path(SEN_BAD)));
    }

    @Test
    public void rijesh() {
        print(EOL + "rijesh()");
        print(new SharkOpenflow(path(RIJESH_18)));
    }

    @Test
    public void shaila() {
        print(EOL + "shaila()");
        MessageFactory.setStrictMessageParsing(true);
        print(new SharkOpenflow(path(SHAILA_MARCH)));
    }

    @Test
    public void julie() {
        print(EOL + "julie()");
        MessageFactory.setStrictMessageParsing(true);
        print(new SharkOpenflow(path(JULIE_PRUNES)));
    }
}
