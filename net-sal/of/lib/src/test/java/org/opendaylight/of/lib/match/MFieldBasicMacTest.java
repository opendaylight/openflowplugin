/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.util.net.MacAddress;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.ARP_SHA;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.ETH_DST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MFieldBasicMac}.
 *
 * @author Simon Hunt
 */
public class MFieldBasicMacTest extends AbstractMatchTest {

    private static final MacAddress MAC_AA = mac("000000:0000aa");
    private static final MacAddress MAC_FF = mac("000000:0000ff");
    private static final MacAddress MAC_MASK = mac("000000:ffffff");

    private static final MfbArpSha ARPSHA_10_AA =
            (MfbArpSha) createBasicField(V_1_0, ARP_SHA, MAC_AA);
    private static final MfbArpSha ARPSHA_10_AA_COPY =
            (MfbArpSha) createBasicField(V_1_0, ARP_SHA, MAC_AA);
    private static final MfbArpSha ARPSHA_13_AA =
            (MfbArpSha) createBasicField(V_1_3, ARP_SHA, MAC_AA);

    private static final MfbEthDst ETHDST_13_AA =
            (MfbEthDst) createBasicField(V_1_3, ETH_DST, MAC_AA);
    private static final MfbEthDst ETHDST_13_FF =
            (MfbEthDst) createBasicField(V_1_3, ETH_DST, MAC_FF);
    private static final MfbEthDst ETHDST_13_FF_MASKED =
            (MfbEthDst) createBasicField(V_1_3, ETH_DST, MAC_FF, MAC_MASK);
    private static final MfbEthDst ETHDST_13_FF_MASKED_COPY =
            (MfbEthDst) createBasicField(V_1_3, ETH_DST, MAC_FF, MAC_MASK);

    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyEqual(ARPSHA_10_AA, ARPSHA_10_AA);
        verifyNotSameButEqual(ARPSHA_10_AA, ARPSHA_10_AA_COPY);
        verifyNotSameButEqual(ETHDST_13_FF_MASKED, ETHDST_13_FF_MASKED_COPY);
    }

    @Test
    public void diffVersSameFieldValue() {
        print(EOL + "diffVersSameFieldValue()");
        verifyNotSameButEqual(ARPSHA_10_AA, ARPSHA_13_AA);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(ETHDST_13_AA, ETHDST_13_FF);
    }

    @Test
    public void maskNoMask() {
        print(EOL + "maskNoMask()");
        verifyNotEqual(ETHDST_13_FF, ETHDST_13_FF_MASKED);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(ARPSHA_10_AA, ETHDST_13_AA);
        verifyNotEqual(ARPSHA_10_AA, OTHER_FIELD);
    }
}
