/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.singlelayer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.Uint8;

public class SingleLayerFlowServiceTest extends ServiceMocking {
    private static final Uint8 TABLE_ID = Uint8.valueOf(42);
    private SingleLayerFlowService<AddFlowOutput> service;

    @Override
    protected void setup() {
        service = new SingleLayerFlowService<>(mockedRequestContextStack, mockedDeviceContext, AddFlowOutput.class);
    }

    @Test
    public void buildRequest() {
        final AddFlowInput input = new AddFlowInputBuilder()
                .setTableId(TABLE_ID)
                .build();

        final OfHeader ofHeader = service.buildRequest(DUMMY_XID, input);
        assertEquals(FlowMessage.class, ofHeader.implementedInterface());

        final FlowMessage result = (FlowMessage) ofHeader;

        assertEquals(FlowModCommand.OFPFCADD, result.getCommand());
        assertEquals(TABLE_ID, result.getTableId());
    }
}
