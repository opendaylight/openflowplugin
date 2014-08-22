/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.VersionNotSupportedException;

import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.FEATURES_REQUEST;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit test for the OfmFeaturesRequest message.
 *
 * @author Simon Hunt
 */
public class OfmFeaturesRequestTest extends OfmTest {

    // test files
    private static final String TF_FREQ_10 = "v10/featuresRequest";
    private static final String TF_FREQ_11 = "v11/featuresRequest";
    private static final String TF_FREQ_12 = "v12/featuresRequest";
    private static final String TF_FREQ_13 = "v13/featuresRequest";

    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void featuresRequest13() {
        print(EOL + "featuresRequest13()");
        verifyMsgHeader(TF_FREQ_13, V_1_3, FEATURES_REQUEST, 8);
    }

    @Test
    public void featuresRequest12() {
        print(EOL + "featuresRequest12()");
        verifyNotSupported(TF_FREQ_12);
    }

    @Test
    public void featuresRequest11() {
        print(EOL + "featuresRequest11()");
        verifyNotSupported(TF_FREQ_11);
    }

    @Test
    public void featuresRequest10() {
        print(EOL + "featuresRequest10()");
        verifyMsgHeader(TF_FREQ_10, V_1_0, FEATURES_REQUEST, 8);
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeFeaturesRequest13() {
        print(EOL + "encodeFeaturesRequest13()");
        mm = MessageFactory.create(V_1_3, FEATURES_REQUEST);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, FEATURES_REQUEST, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_FREQ_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeFeaturesRequest12() {
        mm = MessageFactory.create(V_1_2, FEATURES_REQUEST);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeFeaturesRequest11() {
        mm = MessageFactory.create(V_1_1, FEATURES_REQUEST);
    }

    @Test
    public void encodeFeaturesRequest10() {
        print(EOL + "encodeFeaturesRequest10()");
        mm = MessageFactory.create(V_1_0, FEATURES_REQUEST);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, FEATURES_REQUEST, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_FREQ_10);
    }

}
