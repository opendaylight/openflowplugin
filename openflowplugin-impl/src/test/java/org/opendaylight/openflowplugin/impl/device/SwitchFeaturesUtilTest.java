/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Tests for setting switch features for different version of OF plugin.
 *
 * @author jsebin
 */
public class SwitchFeaturesUtilTest {

    private GetFeaturesOutputBuilder featuresOutputBuilder;
    private SwitchFeaturesUtil swUtil;


    /**
     * Initialization of
     * {@link org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
     * .GetFeaturesOutputBuilder GetFeaturesOutputBuilder}
     * and {@link SwitchFeaturesUtil SwitchFeaturesUtil}.
     */
    @Before
    public void setUp() {
        featuresOutputBuilder = new GetFeaturesOutputBuilder();
        swUtil = SwitchFeaturesUtil.getInstance();
    }

    @After
    public void tearDown() {
        featuresOutputBuilder = null;
        swUtil = null;
    }

    /**
     * Test method for
     * {@link SwitchFeaturesUtil#buildSwitchFeatures} for OF 1.0 version
     * and switch feature capabilities.
     * .
     */
    @Test
    public void testSwFeaturesCapabilitiesV10() {
        CapabilitiesV10 capabilities = new CapabilitiesV10(true, false, true, false, true, false, true, false);
        featuresOutputBuilder.setCapabilitiesV10(capabilities).setVersion(Uint8.ONE);

        Assert.assertNotNull(swUtil.buildSwitchFeatures(featuresOutputBuilder.build()));
    }

    /**
     * Test method for
     * {@link SwitchFeaturesUtil#buildSwitchFeatures} for OF 1.3 version
     * and switch feature capabilities
     * .
     */
    @Test
    public void testSwFeaturesCapabilitiesV13() {
        Capabilities capabilities = new Capabilities(true, false, true, false, true, false, true);
        featuresOutputBuilder.setCapabilities(capabilities).setCapabilitiesV10(null).setVersion(Uint8.valueOf(4));

        Assert.assertNotNull(swUtil.buildSwitchFeatures(featuresOutputBuilder.build()));
    }

    /**
     * Test method for
     * {@link SwitchFeaturesUtil#buildSwitchFeatures} for malformed switch feature capabilities
     * - at least one feature is null
     * .
     */
    //@Test TODO:do we need to check if capability is null?
    public void testSwFeaturesCapabilitiesMalformed() {
        CapabilitiesV10 capabilities = new CapabilitiesV10(true, false, true, false, true, false, true, null);
        featuresOutputBuilder.setCapabilitiesV10(capabilities).setCapabilities(null).setVersion(Uint8.ONE);

        Assert.assertNull(swUtil.buildSwitchFeatures(featuresOutputBuilder.build()));
    }

    /**
     * Test method for
     * {@link SwitchFeaturesUtil#buildSwitchFeatures} for mismatch between
     * version and switch feature capabilities
     * .
     */
    @Test
    public void testSwFeaturesCapabilitiesVersionMismatch() {
        CapabilitiesV10 capabilities = new CapabilitiesV10(true, false, true, false, true, false, true, false);
        featuresOutputBuilder.setCapabilitiesV10(capabilities).setCapabilities(null).setVersion(Uint8.valueOf(4));

        Assert.assertNull(swUtil.buildSwitchFeatures(featuresOutputBuilder.build()));
    }

    /**
     * Test method for
     * {@link SwitchFeaturesUtil#buildSwitchFeatures} for nonexisting version
     * .
     */
    @Test
    public void testSwFeaturesCapabilitiesNonexistingVersion() {
        CapabilitiesV10 capabilities = new CapabilitiesV10(true, false, true, false, true, false, true, false);
        featuresOutputBuilder.setCapabilitiesV10(capabilities).setCapabilities(null).setVersion(Uint8.ZERO);

        Assert.assertNull(swUtil.buildSwitchFeatures(featuresOutputBuilder.build()));
    }
}
