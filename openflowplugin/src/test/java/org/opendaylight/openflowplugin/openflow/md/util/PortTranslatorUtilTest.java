package org.opendaylight.openflowplugin.openflow.md.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;

/**
 * Created by Martin Bobak mbobak@cisco.com on 7/29/14.
 */
public class PortTranslatorUtilTest {



    /**
     * Test  method for {@link org.opendaylight.openflowplugin.openflow.md.util.PortTranslatorUtil#translatePortFeatures(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures)}
     */
    @Test
    public void testTranslatePortFeatures() {

        Boolean[] bls = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};

        for (int i = 0; i < 16; i++) {
            bls[i] = true;
            final PortFeatures apf = new PortFeatures(bls[0],bls[1],bls[2],bls[3],bls[4],bls[5],bls[6],bls[7],bls[8],
                    bls[9],bls[10],bls[11],bls[12],bls[13],bls[14],bls[15]);

            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures npf = PortTranslatorUtil.translatePortFeatures(apf);
            assertEqualsPortFeatures(apf, npf);
            bls[i] = false;
        }

    }

    private void assertEqualsPortFeatures(PortFeatures apf, org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures npf) {
        assertEquals(apf.is_100gbFd(), npf.isHundredGbFd());
        assertEquals(apf.is_100mbFd(), npf.isHundredMbFd());
        assertEquals(apf.is_100mbHd(), npf.isHundredMbHd());

        assertEquals(apf.is_10gbFd(), npf.isTenGbFd());
        assertEquals(apf.is_10mbFd(), npf.isTenMbFd());
        assertEquals(apf.is_10mbHd(), npf.isTenMbHd());

        assertEquals(apf.is_1gbFd(), npf.isOneGbFd());
        assertEquals(apf.is_1gbHd(), npf.isOneGbHd());
        assertEquals(apf.is_1tbFd(), npf.isOneTbFd());

        assertEquals(apf.is_40gbFd(), npf.isFortyGbFd());

        assertEquals(apf.isAutoneg(), npf.isAutoeng());
        assertEquals(apf.isCopper(), npf.isCopper());
        assertEquals(apf.isFiber(), npf.isFiber());
        assertEquals(apf.isOther(), npf.isOther());
        assertEquals(apf.isPause(), npf.isPause());
        assertEquals(apf.isPauseAsym(), npf.isPauseAsym());
    }

    /**
     * Test  method for {@link org.opendaylight.openflowplugin.openflow.md.util.PortTranslatorUtil#translatePortFeatures(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10)}
     */
    @Test
    public void testTranslatePortFeaturesV10() {

        boolean[] bls = {false,false,false,false,false,false,false,false,false,false,false,false};

        for (int i = 0; i <12; i++) {

            bls[i] = true;

            final PortFeaturesV10 apfV10 = new PortFeaturesV10(bls[0],bls[1],bls[2],bls[3],bls[4],bls[5],bls[6],
                    bls[7],bls[8],bls[9],bls[10],bls[11]);
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures npf = PortTranslatorUtil.translatePortFeatures(apfV10);
            assertEqualsPortFeaturesV10(apfV10, npf);
            bls[i] = false;

        }

    }

    private void assertEqualsPortFeaturesV10(PortFeaturesV10 apfV10, org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures npf) {
        assertEquals(apfV10.is_100mbFd(), npf.isHundredMbFd());
        assertEquals(apfV10.is_100mbHd(), npf.isHundredMbHd());

        assertEquals(apfV10.is_10gbFd(), npf.isTenGbFd());
        assertEquals(apfV10.is_10mbFd(), npf.isTenMbFd());
        assertEquals(apfV10.is_10mbHd(), npf.isTenMbHd());

        assertEquals(apfV10.is_1gbFd(), npf.isOneGbFd());
        assertEquals(apfV10.is_1gbHd(), npf.isOneGbHd());

        assertEquals(apfV10.isAutoneg(), npf.isAutoeng());
        assertEquals(apfV10.isCopper(), npf.isCopper());
        assertEquals(apfV10.isFiber(), npf.isFiber());
        assertEquals(apfV10.isPause(), npf.isPause());
        assertEquals(apfV10.isPauseAsym(), npf.isPauseAsym());
    }

}
