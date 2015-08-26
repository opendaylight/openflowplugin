/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.translator;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;

/**
 * translate {@link FlowRemoved} message to FlowRemoved notification (omit instructions)
 */
public class FlowRemovedTranslator implements MessageTranslator<FlowRemoved, org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved> {

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved translate(FlowRemoved input, DeviceContext deviceContext, Object connectionDistinguisher) {
        FlowRemovedBuilder flowRemovedBld = new FlowRemovedBuilder()
                .setMatch(translateMatch(input, deviceContext).build())
                .setCookie(new FlowCookie(input.getCookie()))
                .setNode(new NodeRef(deviceContext.getDeviceState().getNodeInstanceIdentifier()))
                .setPriority(input.getPriority())
                .setTableId(input.getTableId().getValue().shortValue());

        return flowRemovedBld.build();
    }

    protected MatchBuilder translateMatch(FlowRemoved flowRemoved, DeviceContext deviceContext) {
        return MatchConvertorImpl.fromOFMatchToSALMatch(flowRemoved.getMatch(),
                deviceContext.getDeviceState().getFeatures().getDatapathId(), OpenflowVersion.OF13);
    }
}
