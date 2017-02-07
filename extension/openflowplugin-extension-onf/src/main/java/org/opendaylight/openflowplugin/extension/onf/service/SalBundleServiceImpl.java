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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.Messages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageSalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControlSalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.bundle.add.message.sal.SalAddMessageDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.bundle.control.sal.SalControlDataBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Simple bundle extension service.
 */
public class SalBundleServiceImpl implements SalBundleService {

    private final SalExperimenterMessageService experimenterMessageService;

    public SalBundleServiceImpl(final SalExperimenterMessageService experimenterMessageService) {
        this.experimenterMessageService = Preconditions.checkNotNull(experimenterMessageService,
                "SalExperimenterMessageService can not be null!");
    }

    @Override
    public Future<RpcResult<Void>> controlBundle(ControlBundleInput input) {
        final SendExperimenterInputBuilder experimenterInputBuilder = new SendExperimenterInputBuilder();
        experimenterInputBuilder.setNode(input.getNode());
        experimenterInputBuilder.setExperimenterMessageOfChoice(
                new BundleControlSalBuilder()
                        .setSalControlData(
                                new SalControlDataBuilder(input).build()
                        )
                        .build()
        );
        return experimenterMessageService.sendExperimenter(experimenterInputBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> addBundleMessages(AddBundleMessagesInput input) {
        final List<ListenableFuture<RpcResult<Void>>> partialResults = new ArrayList<>();
        final SendExperimenterInputBuilder experimenterInputBuilder = new SendExperimenterInputBuilder();
        final BundleAddMessageSalBuilder bundleAddMessageBuilder = new BundleAddMessageSalBuilder();
        final SalAddMessageDataBuilder dataBuilder = new SalAddMessageDataBuilder();
        experimenterInputBuilder.setNode(input.getNode());
        dataBuilder.setBundleId(input.getBundleId());
        dataBuilder.setFlags(input.getFlags());
        dataBuilder.setBundleProperty(input.getBundleProperty());
        for (Messages message : input.getMessages()) {
            dataBuilder.setBundleInnerMessage(message.getBundleInnerMessage());
            experimenterInputBuilder.setExperimenterMessageOfChoice(bundleAddMessageBuilder.setSalAddMessageData(dataBuilder.build()).build());
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
