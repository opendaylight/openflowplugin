/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.OfmMultipartReply;
import org.opendaylight.of.lib.msg.OfmMutableMultipartRequest;
import org.opendaylight.of.lib.msg.OpenflowMessage;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for CfgDesc.
 *
 * @author Simon Hunt
 */
public class CfgDescTest {

    private static final String E = "";

    private static final String MFR = "Acme Co.";
    private static final String HW = "FlibberGibbet-01";
    private static final String SW = "Fortran 7.1w";
    private static final String SERIAL = "Korn-123-Pops";
    private static final String DP = "Somewhere in the wiring closet";

    // seed MP/DESC request
    private static OpenflowMessage REQUEST;

    @BeforeClass
    public static void classSetUp() {
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.type(MultipartType.DESC);
        REQUEST = req.toImmutable();
    }

    private CfgDesc desc;

    private void verifyDesc(CfgDesc desc, String expMfr, String expHw,
                            String expSw, String expSerial, String expDp) {
        print(desc + EOL);
        assertEquals(AM_NEQ, expMfr, desc.getMfrDesc());
        assertEquals(AM_NEQ, expHw, desc.getHwDesc());
        assertEquals(AM_NEQ, expSw, desc.getSwDesc());
        assertEquals(AM_NEQ, expSerial, desc.getSerialNum());
        assertEquals(AM_NEQ, expDp, desc.getDpDesc());

        // now use the desc to generate a reply
        OfmMultipartReply rep =
                (OfmMultipartReply) desc.createMpDescReply(REQUEST);
        print(rep.toDebugString());
        MBodyDesc d = (MBodyDesc) rep.getBody();

        assertEquals(AM_NEQ, expMfr, d.getMfrDesc());
        assertEquals(AM_NEQ, expHw, d.getHwDesc());
        assertEquals(AM_NEQ, expSw, d.getSwDesc());
        assertEquals(AM_NEQ, expSerial, d.getSerialNum());
        assertEquals(AM_NEQ, expDp, d.getDpDesc());
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        desc = new CfgDesc();
        verifyDesc(desc, E, E, E, E, E);
    }

    @Test
    public void mfrSerial() {
        print(EOL + "mfrSerial()");
        desc = new CfgDesc();
        desc.setDesc(SwitchDefn.Keyword.DESC_MFR, MFR);
        desc.setDesc(SwitchDefn.Keyword.DESC_SERIAL, SERIAL);
        verifyDesc(desc, MFR, E, E, SERIAL, E);
    }

    @Test
    public void allFields() {
        print(EOL + "allFields()");
        desc = new CfgDesc();
        desc.setDesc(SwitchDefn.Keyword.DESC_MFR, MFR);
        desc.setDesc(SwitchDefn.Keyword.DESC_HW, HW);
        desc.setDesc(SwitchDefn.Keyword.DESC_SW, SW);
        desc.setDesc(SwitchDefn.Keyword.DESC_SERIAL, SERIAL);
        desc.setDesc(SwitchDefn.Keyword.DESC_DP, DP);
        verifyDesc(desc, MFR, HW, SW, SERIAL, DP);
    }
}
