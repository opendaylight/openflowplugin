/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessageBuilder;
/**
 * @author jsebin
 */
public class PortFeaturesUtilTest {

    private PortStatusMessageBuilder portStatusMessageBuilder;
    private PortFeaturesUtil portUtil;


    /**
     * initialization of {@link org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessageBuilder.PortStatusMessageBuilder}
     * and {@link PortFeaturesUtil}
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        portStatusMessageBuilder = new PortStatusMessageBuilder();
        portUtil = PortFeaturesUtil.getInstance();
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        portStatusMessageBuilder = null;
        portUtil = null;
    }

    /**
     * Test method for
     * {@link PortFeaturesUtil#getPortBandwidth()} for OF 1.0 version
     * and features
     * .
     */
    @Test
    public void testFeaturesV10() {
        PortFeaturesV10 features = new PortFeaturesV10(true, true, true, false, true, false, true, true, true, false, true, false);
        portStatusMessageBuilder.setReason(PortReason.OFPPRMODIFY).setVersion((short) 1).setCurrentFeaturesV10(features);

        Assert.assertNotNull(portUtil.getPortBandwidth(portStatusMessageBuilder.build()));
    }

    /**
     * Test method for
     * {@link PortFeaturesUtil#getPortBandwidth()} for OF 1.3 version
     * and features
     * .
     */
    @Test
    public void testFeaturesV13() {
        PortFeatures features = new PortFeatures(true, true, true, false, true, false, true, true, true, false, true, false, false, true, false, false);
        portStatusMessageBuilder.setReason(PortReason.OFPPRMODIFY).setVersion((short) 4).setCurrentFeatures(features);

        Assert.assertNotNull(portUtil.getPortBandwidth(portStatusMessageBuilder.build()));
    }

    /**
     * Test method for
     * {@link PortFeaturesUtil#getPortBandwidth()} for malformed features
     * - at least one feature is null
     * .
     */
    @Test
    public void testFeaturesMalformed() {
        PortFeaturesV10 features = new PortFeaturesV10(true, true, true, true, true, true, false, false, false, false, true, null);
        portStatusMessageBuilder.setReason(PortReason.OFPPRMODIFY).setVersion((short) 1).setCurrentFeaturesV10(features);

        Assert.assertNull(portUtil.getPortBandwidth(portStatusMessageBuilder.build()));
    }

    /**
     * Test method for
     * {@link PortFeaturesUtil#getPortBandwidth()} for mismatch between
     * port version and port features
     * .
     */
    @Test
    public void testFeaturesVersionMismatch() {
        PortFeatures features = new PortFeatures(true, true, true, false, true, false, true, true, true, false, true, false, false, true, false, false);
        portStatusMessageBuilder.setReason(PortReason.OFPPRMODIFY).setVersion((short) 1).setCurrentFeatures(features);

        Assert.assertNull(portUtil.getPortBandwidth(portStatusMessageBuilder.build()));
    }

    /**
     * Test method for
     * {@link PortFeaturesUtil#getPortBandwidth()} for nonexisting port version
     * .
     */
    @Test
    public void testFeaturesNonexistingVersion() {
        PortFeatures features = new PortFeatures(true, true, true, false, true, false, true, true, true, false, true, false, false, true, false, false);
        portStatusMessageBuilder.setReason(PortReason.OFPPRMODIFY).setVersion((short) 0).setCurrentFeatures(features);

        Assert.assertNull(portUtil.getPortBandwidth(portStatusMessageBuilder.build()));
    }

}
