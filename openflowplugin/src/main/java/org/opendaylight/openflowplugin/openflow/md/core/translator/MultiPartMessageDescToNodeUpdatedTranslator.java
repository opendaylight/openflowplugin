/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiPartMessageDescToNodeUpdatedTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {
    private static final Logger LOG = LoggerFactory.getLogger(MultiPartMessageDescToNodeUpdatedTranslator.class);
    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie,
            SessionContext sc, OfHeader msg) {
        if(msg instanceof MultipartReply && ((MultipartReply) msg).getType() == MultipartType.OFPMPDESC) {
            LOG.debug("MultipartReplyMessage - MultipartReplyDesc Being translated to NodeUpdated ");
            MultipartReplyMessage message = (MultipartReplyMessage) msg;
            List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
            BigInteger datapathId = sc.getFeatures().getDatapathId();
            NodeUpdatedBuilder builder = InventoryDataServiceUtil.nodeUpdatedBuilderFromDataPathId(datapathId);
            FlowCapableNodeUpdatedBuilder fnub = new FlowCapableNodeUpdatedBuilder();
            MultipartReplyDescCase caseBody = (MultipartReplyDescCase) message.getMultipartReplyBody();
            MultipartReplyDesc body = caseBody.getMultipartReplyDesc();
            fnub.setHardware(body.getHwDesc());
            fnub.setManufacturer(body.getMfrDesc());
            fnub.setSerialNumber(body.getSerialNum());
            fnub.setDescription(body.getDpDesc());
            fnub.setSoftware(body.getSwDesc());
            builder.addAugmentation(FlowCapableNodeUpdated.class, fnub.build());
            NodeUpdated nodeUpdated = builder.build();
            list.add(nodeUpdated);
            return list;
        } else {
            return Collections.emptyList();
        }
    }

}
