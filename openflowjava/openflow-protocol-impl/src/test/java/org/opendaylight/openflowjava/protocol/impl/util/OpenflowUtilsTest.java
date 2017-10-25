/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;

/**
 * @author michal.polkorab
 *
 */
public class OpenflowUtilsTest {

    /**
     * Tests {@link OpenflowUtils#createPortState(long)}
     */
    @Test
    public void testPortState() {
        PortStateV10 state = OpenflowUtils.createPortState(512L);
        Assert.assertEquals("Wrong port state",
                new PortStateV10(false, false, false, false, true, false, true, false), state);

        state = OpenflowUtils.createPortState(1793L);
        Assert.assertEquals("Wrong port state",
                new PortStateV10(false, true, false, true, true, true, false, true), state);

        state = OpenflowUtils.createPortState(1L);
        Assert.assertEquals("Wrong port state",
                new PortStateV10(false, true, false, false, false, false, true, false), state);
    }

    /**
     * Tests {@link OpenflowUtils#createPortConfig(long)}
     */
    @Test
    public void testPortConfig() {
        PortConfigV10 config = OpenflowUtils.createPortConfig(127L);
        Assert.assertEquals("Wrong port config",
                new PortConfigV10(true, true, true, true, true, true, true), config);

        config = OpenflowUtils.createPortConfig(0L);
        Assert.assertEquals("Wrong port config",
                new PortConfigV10(false, false, false, false, false, false, false), config);
    }

    /**
     * Tests {@link OpenflowUtils#createPortFeatures(long)}
     */
    @Test
    public void testPortFeatures() {
        PortFeaturesV10 features = OpenflowUtils.createPortFeatures(4095L);
        Assert.assertEquals("Wrong port features", new PortFeaturesV10(true, true, true, true, true, true, true,
                true, true, true, true, true), features);

        features = OpenflowUtils.createPortFeatures(0L);
        Assert.assertEquals("Wrong port features", new PortFeaturesV10(false, false, false, false, false, false,
                false, false, false, false, false, false), features);
    }
}