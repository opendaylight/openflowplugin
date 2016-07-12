/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.flowflag;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;

/**
 * match conversion and injection test
 */
public class FlowFlagReactorTest {

    private FlowModFlags[] flowFlags;
    private ConvertorManager convertorManager;

    /**
     * prepare input match
     */
    @Before
    public void setUp() {
        flowFlags = new FlowModFlags[] {
                new FlowModFlags(true, true, true, true, true),
                new FlowModFlags(false, false, false, false, false),
                new FlowModFlags(true, false, true, false, true)
        };
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    /**
     * convert for OF-1.3, inject into {@link FlowModInputBuilder}
     */
    @Test
    public void testMatchConvertorV13_flow() {
        FlowModInputBuilder target = new FlowModInputBuilder();
        for (FlowModFlags fFlag : flowFlags) {
            target.setFlags(null);
            FlowFlagReactor.getInstance().convert(fFlag,
                    OFConstants.OFP_VERSION_1_3, target, convertorManager);
            Assert.assertNotNull(target.getFlags());
        }
    }

    /**
     * convert for OF-1.0, inject into {@link FlowModInputBuilder}
     */
    @Test
    public void testMatchConvertorV10_flow() {
        FlowModInputBuilder target = new FlowModInputBuilder();
        for (FlowModFlags fFlag : flowFlags) {
            target.setFlagsV10(null);
            FlowFlagReactor.getInstance().convert(fFlag,
                    OFConstants.OFP_VERSION_1_0, target, convertorManager);
            Assert.assertNotNull(target.getFlagsV10());
        }
    }
}