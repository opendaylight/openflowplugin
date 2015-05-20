/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.listener;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * openflowplugin-api
 * org.opendaylight.openflowplugin.impl.openflow.device
 *
 * Implementation for {@link MultiMsgCollector} interface
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * @author <a href="mailto:tkubas@cisco.com">Timotej Kubas</a>
 *         </p>
 *         Created: Mar 23, 2015
 */
public class MultiMsgCollectorImpl implements MultiMsgCollector {

    private static final Logger LOG = LoggerFactory.getLogger(MultiMsgCollectorImpl.class);

    private final List<MultipartReply> replyCollection = new ArrayList<>();
    private final RequestContext<List<MultipartReply>> requestContext;
    private final DeviceReplyProcessor deviceReplyProcessor;
    private MultipartType msgType;

    public MultiMsgCollectorImpl(final DeviceReplyProcessor deviceReplyProcessor, final RequestContext<List<MultipartReply>> requestContext) {
        this.deviceReplyProcessor = Preconditions.checkNotNull(deviceReplyProcessor);
        this.requestContext = Preconditions.checkNotNull(requestContext);
    }

    @Override
    public void addMultipartMsg(final MultipartReply reply) {
        Preconditions.checkNotNull(reply);
        LOG.trace("Try to add Multipart reply msg with XID {}", reply.getXid());

        if (msgType == null) {
            msgType = reply.getType();
        }

        if (msgType.equals(reply.getType())) {
            LOG.warn("MultiMsgCollector get incorrect multipart msg with type {} but expected type is {}", reply.getType(), msgType);
        }

        replyCollection.add(reply);
        if (!reply.getFlags().isOFPMPFREQMORE()) {
            final RpcResult<List<MultipartReply>> rpcResult = RpcResultBuilder.success(replyCollection).build();
            requestContext.setResult(rpcResult);
            requestContext.close();
            deviceReplyProcessor.processReply(requestContext.getXid(), replyCollection);
        }
    }
}
