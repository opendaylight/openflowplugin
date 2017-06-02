/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.translator;

import java.util.Collections;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.util.PortTranslatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tkubas
 */
public class PortUpdateTranslator implements MessageTranslator<PortGrouping, FlowCapableNodeConnector> {
    private static final Logger LOG = LoggerFactory.getLogger(PortUpdateTranslator.class);

    @Override
    public FlowCapableNodeConnector translate(final PortGrouping input,
                                              final DeviceInfo deviceInfo, final Object connectionDistinguisher) {
        final FlowCapableNodeConnectorBuilder builder = new FlowCapableNodeConnectorBuilder();
        //OF1.0
        if (deviceInfo.getVersion() == OFConstants.OFP_VERSION_1_0) {
            builder.setAdvertisedFeatures(PortTranslatorUtil.translatePortFeatures(input.getAdvertisedFeaturesV10()));
            builder.setConfiguration(PortTranslatorUtil.translatePortConfig(input.getConfigV10()));
            builder.setCurrentFeature(PortTranslatorUtil.translatePortFeatures(input.getCurrentFeaturesV10()));
            builder.setPeerFeatures(PortTranslatorUtil.translatePortFeatures(input.getPeerFeaturesV10()));
            builder.setState(PortTranslatorUtil.translatePortState(input.getStateV10()));
            builder.setSupported(PortTranslatorUtil.translatePortFeatures(input.getSupportedFeaturesV10()));
        } else if (deviceInfo.getVersion() == OFConstants.OFP_VERSION_1_3) {
            builder.setAdvertisedFeatures(PortTranslatorUtil.translatePortFeatures(input.getAdvertisedFeatures()));
            builder.setConfiguration(PortTranslatorUtil.translatePortConfig(input.getConfig()));
            builder.setCurrentFeature(PortTranslatorUtil.translatePortFeatures(input.getCurrentFeatures()));
            builder.setPeerFeatures(PortTranslatorUtil.translatePortFeatures(input.getPeerFeatures()));
            builder.setState(PortTranslatorUtil.translatePortState(input.getState()));
            builder.setSupported(PortTranslatorUtil.translatePortFeatures(input.getSupportedFeatures()));
            builder.setQueue(Collections.<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue>emptyList());
        }
        if (input instanceof PortStatusMessage) {
            if (((PortStatusMessage) input).getReason() != null) {
                builder.setReason(PortReason.forValue(((PortStatusMessage) input).getReason().getIntValue()));
            }else {
                LOG.debug("PortStatus Message has reason as null");
            }
        }
        builder.setCurrentSpeed(input.getCurrSpeed());
        builder.setHardwareAddress(input.getHwAddr());
        builder.setMaximumSpeed(input.getMaxSpeed());
        builder.setName(input.getName());
        builder.setPortNumber(new PortNumberUni(input.getPortNo()));

        return builder.build();
    }
}
