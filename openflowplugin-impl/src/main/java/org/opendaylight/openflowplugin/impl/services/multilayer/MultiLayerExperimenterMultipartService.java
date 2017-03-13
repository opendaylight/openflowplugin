/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.multilayer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.extension.api.exception.ConverterNotFoundException;
import org.opendaylight.openflowplugin.extension.api.path.MessagePath;
import org.opendaylight.openflowplugin.impl.services.AbstractExperimenterMultipartService;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.send.experimenter.mp.request.output.ExperimenterCoreMessageItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.send.experimenter.mp.request.output.ExperimenterCoreMessageItemBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.experimenter._case.MultipartReplyExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.experimenter._case.MultipartRequestExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class MultiLayerExperimenterMultipartService extends AbstractExperimenterMultipartService<MultipartReply> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MultiLayerExperimenterMultipartService.class);

    public MultiLayerExperimenterMultipartService(RequestContextStack requestContextStack, DeviceContext deviceContext,
                                                  ExtensionConverterProvider extensionConverterProvider) {
        super(requestContextStack, deviceContext, extensionConverterProvider);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected OfHeader buildRequest(Xid xid, SendExperimenterMpRequestInput input) throws ServiceException {
        final TypeVersionKey key = new TypeVersionKey<>(
            input.getExperimenterMessageOfChoice().getImplementedInterface(),
            getVersion());

        final ConvertorMessageToOFJava<ExperimenterMessageOfChoice, ExperimenterDataOfChoice> messageConverter =
            getExtensionConverterProvider().getMessageConverter(key);

        if (Objects.isNull(messageConverter)) {
            throw new ServiceException(new ConverterNotFoundException(key.toString()));
        }

        try {
            return RequestInputUtils
                .createMultipartHeader(MultipartType.OFPMPEXPERIMENTER, xid.getValue(), getVersion())
                .setMultipartRequestBody(new MultipartRequestExperimenterCaseBuilder()
                    .setMultipartRequestExperimenter(new MultipartRequestExperimenterBuilder()
                        .setExperimenter(messageConverter.getExperimenterId())
                        .setExpType(messageConverter.getType())
                        .setExperimenterDataOfChoice(messageConverter
                            .convert(input.getExperimenterMessageOfChoice()))
                        .build())
                    .build())
                .build();
        } catch (final ConversionException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Future<RpcResult<SendExperimenterMpRequestOutput>> handleAndReply(SendExperimenterMpRequestInput input) {
        final ListenableFuture<RpcResult<List<MultipartReply>>> multipartFuture = handleServiceCall(input);
        final SettableFuture<RpcResult<SendExperimenterMpRequestOutput>> finalFuture = SettableFuture.create();

        class CallBackImpl implements FutureCallback<RpcResult<List<MultipartReply>>> {
            @Override
            public void onSuccess(final RpcResult<List<MultipartReply>> result) {
                if (result.isSuccessful()) {
                    final List<MultipartReply> multipartReplies = result.getResult();
                    if (multipartReplies.isEmpty()) {
                        LOG.warn("Multipart reply to Experimenter-Mp request shouldn't be empty list.");
                        finalFuture.set(RpcResultBuilder.<SendExperimenterMpRequestOutput>failed().withError(ErrorType.RPC, "Multipart reply list is empty.").build());
                    } else {
                        LOG.debug(
                                "OnSuccess, rpc result successful, multipart response for rpc sendExperimenterMpRequest with xid {} obtained.",
                                multipartReplies.get(0).getXid());
                        final SendExperimenterMpRequestOutputBuilder sendExpMpReqOutputBuilder = new SendExperimenterMpRequestOutputBuilder();
                        final List<ExperimenterCoreMessageItem> expCoreMessageItem = new ArrayList<>();
                        for(MultipartReply multipartReply : multipartReplies){
                            final MultipartReplyExperimenterCase caseBody = (MultipartReplyExperimenterCase)multipartReply.getMultipartReplyBody();
                            final MultipartReplyExperimenter replyBody = caseBody.getMultipartReplyExperimenter();
                            final ExperimenterDataOfChoice vendorData = replyBody.getExperimenterDataOfChoice();
                            final MessageTypeKey<? extends ExperimenterDataOfChoice> key = new MessageTypeKey<>(
                                    getVersion(),
                                    (Class<? extends ExperimenterDataOfChoice>) vendorData.getImplementedInterface());
                            final ConvertorMessageFromOFJava<ExperimenterDataOfChoice, MessagePath> messageConverter =
                                    getExtensionConverterProvider().getMessageConverter(key);
                            if (messageConverter == null) {
                                LOG.warn("Custom converter for {}[OF:{}] not found",
                                        vendorData.getImplementedInterface(),
                                        getVersion());
                                finalFuture.set(RpcResultBuilder.<SendExperimenterMpRequestOutput>failed().withError(ErrorType.RPC, "Custom converter not found.").build());
                                return;
                            }
                            try {
                                final ExperimenterMessageOfChoice messageOfChoice = messageConverter.convert(vendorData, MessagePath.MPMESSAGE_RPC_OUTPUT);
                                final ExperimenterCoreMessageItemBuilder expCoreMessageItemBuilder = new ExperimenterCoreMessageItemBuilder();
                                expCoreMessageItemBuilder.setExperimenterMessageOfChoice(messageOfChoice);
                                expCoreMessageItem.add(expCoreMessageItemBuilder.build());
                            } catch (final ConversionException e) {
                                LOG.error("Conversion of experimenter message reply failed. Exception: {}", e);
                                finalFuture.set(RpcResultBuilder.<SendExperimenterMpRequestOutput>failed().withError(ErrorType.RPC, "Conversion of experimenter rpc output failed.").build());
                                return;
                            }
                        }
                        sendExpMpReqOutputBuilder.setExperimenterCoreMessageItem(expCoreMessageItem);
                        finalFuture.set(RpcResultBuilder.success(sendExpMpReqOutputBuilder.build()).build());
                    }
                } else {
                    LOG.warn("OnSuccess, rpc result unsuccessful, multipart response for rpc sendExperimenterMpRequest was unsuccessful.");
                    finalFuture.set(RpcResultBuilder.<SendExperimenterMpRequestOutput>failed().withRpcErrors(result.getErrors()).build());
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("Failure multipart response for Experimenter-Mp request. Exception: {}", t);
                finalFuture.set(RpcResultBuilder.<SendExperimenterMpRequestOutput>failed().withError(ErrorType.RPC, "Future error", t).build());
            }
        }

        Futures.addCallback(multipartFuture, new CallBackImpl());

        return finalFuture;
    }

    @VisibleForTesting
    OfHeader buildRequestTest(Xid xid, SendExperimenterMpRequestInput input) throws ServiceException {
        return buildRequest(xid, input);
    }
}
