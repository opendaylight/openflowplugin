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
import org.opendaylight.openflowplugin.openflow.md.util.PortTranslatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.MultipartReplyPortDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiPartReplyPortToNodeConnectorUpdatedTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {
    private static final Logger LOG = LoggerFactory.getLogger(MultiPartReplyPortToNodeConnectorUpdatedTranslator.class);
    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie,
            SessionContext sc, OfHeader msg) {
        if(msg instanceof MultipartReply && ((MultipartReply) msg).getType() == MultipartType.OFPMPPORTDESC) {
            BigInteger datapathId = sc.getFeatures().getDatapathId();
            LOG.trace("MultiPartReplyPortToNodeConnectorUpdatedTranslator Being translated to NodeConnectorUpdated ");
            MultipartReplyMessage message = (MultipartReplyMessage) msg;
            MultipartReplyPortDescCase caseBody = (MultipartReplyPortDescCase) message.getMultipartReplyBody();
            MultipartReplyPortDesc body = caseBody.getMultipartReplyPortDesc();
            List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
            for ( Ports port : body.getPorts() ) {
                LOG.debug("Port: " + port);
                list.add(PortTranslatorUtil.translatePort(msg.getVersion(), datapathId, port.getPortNo(), port));
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }



}
