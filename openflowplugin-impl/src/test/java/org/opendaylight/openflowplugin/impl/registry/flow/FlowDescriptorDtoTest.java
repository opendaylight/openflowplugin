/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;

/**
 * Test for {@link FlowDescriptorFactory.FlowDescriptorDto}.
 */
public class FlowDescriptorDtoTest {

    @Test
    public void testCreate() {
        final FlowDescriptor flowDescriptor = FlowDescriptorFactory.create((short) 1, new FlowId("unit:1"));
        Assert.assertNotNull(flowDescriptor);
        Assert.assertNotNull(flowDescriptor.getFlowId());
        Assert.assertNotNull(flowDescriptor.getTableKey());
    }

    @Test(expected = Exception.class)
    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION") // that is the point of this test
    public void testCreateNegative1() {
        FlowDescriptorFactory.create((short) 1, null);
    }
}
