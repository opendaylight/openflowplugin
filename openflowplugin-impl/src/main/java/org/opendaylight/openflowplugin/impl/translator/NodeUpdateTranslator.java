/*
 * Copyright (c) 2015 - 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.translator;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/* Created by Gaurav Bhagwani (gaurav.bhagwani@ericsson.com) */

/* Translator for NodeUpdate Yang Notification
 * Converts MultipartReplyDesc to NodeUpdated
 * */

 public class NodeUpdateTranslator implements MessageTranslator<MultipartReplyMessage, NodeUpdated> {
    private static final Logger LOG = LoggerFactory.getLogger(NodeUpdateTranslator.class);
    @Override
    public NodeUpdated translate(final MultipartReplyMessage input,
                                              final DeviceInfo deviceInfo, final Object connectionDistinguisher) {
        final FlowCapableNodeUpdatedBuilder builder = new FlowCapableNodeUpdatedBuilder();
        LOG.debug("MultipartReplyMessage - MultipartReplyDesc Being translated to NodeUpdated ");
        BigInteger datapathId = deviceInfo.getDatapathId();
        NodeUpdatedBuilder nodeUpdatedBuilder = InventoryDataServiceUtil.nodeUpdatedBuilderFromDataPathId(datapathId);
        FlowCapableNodeUpdatedBuilder fnub = new FlowCapableNodeUpdatedBuilder();
        MultipartReplyDescCase caseBody = (MultipartReplyDescCase) input.getMultipartReplyBody();
        MultipartReplyDesc body = caseBody.getMultipartReplyDesc();
        fnub.setHardware(body.getHwDesc());
        fnub.setManufacturer(body.getMfrDesc());
        fnub.setSerialNumber(body.getSerialNum());
        fnub.setDescription(body.getDpDesc());
        fnub.setSoftware(body.getSwDesc());
        nodeUpdatedBuilder.addAugmentation(FlowCapableNodeUpdated.class, fnub.build());
        NodeUpdated nodeUpdated = nodeUpdatedBuilder.build();
        return nodeUpdated;
    }
}

