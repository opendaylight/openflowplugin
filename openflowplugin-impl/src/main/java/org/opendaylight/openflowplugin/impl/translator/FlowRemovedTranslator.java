/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.translator;

import java.util.Objects;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.RemovedFlowReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * translate {@link FlowRemoved} message to FlowRemoved notification (omit instructions)
 */
public class FlowRemovedTranslator implements MessageTranslator<FlowRemoved, org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved> {
    private final ConvertorExecutor convertorExecutor;
    private static final Logger LOG = LoggerFactory.getLogger(FlowRemovedTranslator.class);
    public FlowRemovedTranslator(ConvertorExecutor convertorExecutor) {
        this.convertorExecutor = convertorExecutor;
    }

    protected ConvertorExecutor getConvertorExecutor() {
        return convertorExecutor;
    }

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved translate(FlowRemoved input, DeviceInfo deviceInfo, Object connectionDistinguisher) {
        FlowRemovedBuilder flowRemovedBld = new FlowRemovedBuilder()
                .setMatch(translateMatch(input, deviceInfo).build())
                .setCookie(new FlowCookie(input.getCookie()))
                .setNode(new NodeRef(deviceInfo.getNodeInstanceIdentifier()))
                .setPriority(input.getPriority())
                .setTableId(translateTableId(input));

        if(Objects.nonNull(input.getReason())) {
            flowRemovedBld.setReason(translateReason(input));
        }

        return flowRemovedBld.build();
    }

    protected MatchBuilder translateMatch(FlowRemoved flowRemoved, DeviceInfo deviceInfo) {
        final VersionDatapathIdConvertorData datapathIdConvertorData = new VersionDatapathIdConvertorData(deviceInfo.getVersion());
        datapathIdConvertorData.setDatapathId(deviceInfo.getDatapathId());

        final Optional<MatchBuilder> matchBuilderOptional = getConvertorExecutor().convert(
                flowRemoved.getMatch(),
                datapathIdConvertorData);

        return matchBuilderOptional.orElse(new MatchBuilder());
    }

    /**
     * Translate the table ID in the FLOW_REMOVED message to SAL table ID.
     *
     * @param flowRemoved  FLOW_REMOVED message.
     * @return  SAL table ID.
     */
    protected Short translateTableId(FlowRemoved flowRemoved) {
        return flowRemoved.getTableId().getValue().shortValue();
    }


    private RemovedFlowReason translateReason(FlowRemoved removedFlow) {
        LOG.debug("--Entering translateReason within FlowRemovedTranslator with reason:{} " + removedFlow.getReason());
        switch (removedFlow.getReason()) {
            case OFPRRIDLETIMEOUT:
                return RemovedFlowReason.OFPRRIDLETIMEOUT;
            case OFPRRHARDTIMEOUT:
                return RemovedFlowReason.OFPRRHARDTIMEOUT;
            case OFPRRDELETE:
                return RemovedFlowReason.OFPRRDELETE;
            case OFPRRGROUPDELETE:
                return RemovedFlowReason.OFPRRGROUPDELETE;
            default:
                LOG.debug("The flow is being deleted for some unknown reason  ");
                return RemovedFlowReason.OFPRRDELETE;
        }
    }
}
