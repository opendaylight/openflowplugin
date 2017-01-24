/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.service;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SalExperimenterMessageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControlBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple bundle extension service.
 */
public class SalBundleServiceImpl implements SalBundleService {

    private static final Logger LOG = LoggerFactory.getLogger(SalBundleServiceImpl.class);
    private final SalExperimenterMessageService experimenterMessageService;

    public SalBundleServiceImpl(final SalExperimenterMessageService experimenterMessageService) {
        this.experimenterMessageService = Preconditions.checkNotNull(experimenterMessageService,
                "SalExperimenterMessageService can not be null!");
        LOG.info("SalBundleServiceImpl created.");
    }

    @Override
    public Future<RpcResult<Void>> controlBundle(ControlBundleInput input) {
        final SendExperimenterInputBuilder experimenterInputBuilder = new SendExperimenterInputBuilder();
        experimenterInputBuilder.setNode(input.getNode());
        experimenterInputBuilder.setExperimenterMessageOfChoice(new BundleControlBuilder(input).build());
        return experimenterMessageService.sendExperimenter(experimenterInputBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> addBundleMessages(AddBundleMessagesInput input) {
        final List<ListenableFuture<RpcResult<Void>>> partialResults = new ArrayList<>();
        final SendExperimenterInputBuilder experimenterInputBuilder = new SendExperimenterInputBuilder();
        final BundleAddMessageBuilder bundleAddMessageBuilder = new BundleAddMessageBuilder();
        experimenterInputBuilder.setNode(input.getNode());
        bundleAddMessageBuilder.setBundleId(input.getBundleId());
        bundleAddMessageBuilder.setFlags(input.getFlags());
        bundleAddMessageBuilder.setBundleProperty(input.getBundleProperty());
        for (Message message : input.getMessage()) {
            bundleAddMessageBuilder.setBundleInnerMessage(message.getBundleInnerMessage());
            experimenterInputBuilder.setExperimenterMessageOfChoice(bundleAddMessageBuilder.build());
            ListenableFuture<RpcResult<Void>> res = JdkFutureAdapters.listenInPoolThread(
                    experimenterMessageService.sendExperimenter(experimenterInputBuilder.build()));
            partialResults.add(res);
        }
        return processResults(partialResults);
    }

    private static Future<RpcResult<Void>> processResults(final List<ListenableFuture<RpcResult<Void>>> partialResults) {
        final SettableFuture<RpcResult<Void>> result = SettableFuture.create();
        Futures.addCallback(Futures.successfulAsList(partialResults), new FutureCallback<List<RpcResult<Void>>>() {
            @Override
            public void onSuccess(List<RpcResult<Void>> results) {
                final ArrayList<RpcError> errors = new ArrayList<>();
                final RpcResultBuilder<Void> rpcResultBuilder;
                for (RpcResult<Void> res : results) {
                    if (res == null) {
                        errors.add(RpcResultBuilder.newError(RpcError.ErrorType.APPLICATION, "BundleExtensionService",
                                "RpcResult is null."));
                    } else if (!res.isSuccessful()) {
                        errors.addAll(res.getErrors());
                    }
                }
                if (errors.isEmpty()) {
                    rpcResultBuilder = RpcResultBuilder.success();
                } else {
                    rpcResultBuilder = RpcResultBuilder.<Void>failed().withRpcErrors(errors);
                }
                result.set(rpcResultBuilder.build());
            }
            @Override
            public void onFailure(Throwable t) {
                RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.failed();
                result.set(rpcResultBuilder.build());
            }
        });
        return result;
    }

}
