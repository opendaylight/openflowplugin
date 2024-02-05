/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundle;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageSalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControlSalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.bundle.add.message.sal.SalAddMessageDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.bundle.control.sal.SalControlDataBuilder;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple bundle extension service.
 */
public class SalBundleRpcs {
    private static final Logger LOG = LoggerFactory.getLogger(SalBundleRpcs.class);

    private final SalExperimenterMessageRpc experimenterMessageRpc;

    public SalBundleRpcs(final SalExperimenterMessageRpc experimenterMessageRpc) {
        this.experimenterMessageRpc = requireNonNull(experimenterMessageRpc,
            "SalExperimenterMessageRpc can not be null!");
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<ControlBundleOutput>> controlBundle(final ControlBundleInput input) {
        LOG.debug("Control message for device {} and bundle type {}", input.getNode(), input.getType());
        final SendExperimenterInputBuilder experimenterInputBuilder = new SendExperimenterInputBuilder();
        experimenterInputBuilder.setNode(input.getNode());
        experimenterInputBuilder.setExperimenterMessageOfChoice(
                new BundleControlSalBuilder().setSalControlData(new SalControlDataBuilder(input).build()).build());
        return Futures.transform(experimenterMessageRpc.sendExperimenter(experimenterInputBuilder.build()),
            sendExperimenterOutputRpcResult -> {
                if (sendExperimenterOutputRpcResult.isSuccessful()) {
                    return RpcResultBuilder.<ControlBundleOutput>success().build();
                } else {
                    return RpcResultBuilder.<ControlBundleOutput>failed().build();
                }
            }, MoreExecutors.directExecutor());
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<AddBundleMessagesOutput>> addBundleMessages(final AddBundleMessagesInput input) {
        final List<ListenableFuture<RpcResult<SendExperimenterOutput>>> partialResults = new ArrayList<>();
        final SendExperimenterInputBuilder experimenterInputBuilder = new SendExperimenterInputBuilder();
        final BundleAddMessageSalBuilder bundleAddMessageBuilder = new BundleAddMessageSalBuilder();
        final SalAddMessageDataBuilder dataBuilder = new SalAddMessageDataBuilder();
        experimenterInputBuilder.setNode(input.getNode());
        dataBuilder.setNode(input.getNode());
        dataBuilder.setBundleId(input.getBundleId());
        dataBuilder.setFlags(input.getFlags());
        dataBuilder.setBundleProperty(input.getBundleProperty());
        for (Message message : input.getMessages().getMessage()) {
            dataBuilder.setBundleInnerMessage(message.getBundleInnerMessage());
            experimenterInputBuilder.setExperimenterMessageOfChoice(
                    bundleAddMessageBuilder.setSalAddMessageData(dataBuilder.build()).build());
            partialResults.add(experimenterMessageRpc.sendExperimenter(experimenterInputBuilder.build()));
        }
        return processResults(partialResults);
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(ControlBundle.class, this::controlBundle)
            .put(AddBundleMessages.class, this::addBundleMessages)
            .build();
    }

    private static ListenableFuture<RpcResult<AddBundleMessagesOutput>> processResults(
            final List<ListenableFuture<RpcResult<SendExperimenterOutput>>> partialResults) {
        final SettableFuture<RpcResult<AddBundleMessagesOutput>> result = SettableFuture.create();
        Futures.addCallback(Futures.successfulAsList(partialResults),new FutureCallback<
                List<RpcResult<SendExperimenterOutput>>>() {
            @Override
            public void onSuccess(final List<RpcResult<SendExperimenterOutput>> results) {
                final ArrayList<RpcError> errors = new ArrayList<>();
                final RpcResultBuilder<AddBundleMessagesOutput> rpcResultBuilder;
                for (RpcResult<SendExperimenterOutput> res : results) {
                    if (res == null) {
                        // FIXME: this should never happen
                        errors.add(RpcResultBuilder.newError(ErrorType.APPLICATION,
                            new ErrorTag("BundleExtensionService"), "RpcResult is null."));
                    } else if (!res.isSuccessful()) {
                        errors.addAll(res.getErrors());
                    }
                }
                if (errors.isEmpty()) {
                    rpcResultBuilder = RpcResultBuilder.success();
                } else {
                    rpcResultBuilder = RpcResultBuilder.<AddBundleMessagesOutput>failed().withRpcErrors(errors);
                }
                result.set(rpcResultBuilder.build());
            }

            @Override
            public void onFailure(final Throwable throwable) {
                RpcResultBuilder<AddBundleMessagesOutput> rpcResultBuilder = RpcResultBuilder.failed();
                result.set(rpcResultBuilder.build());
            }
        }, MoreExecutors.directExecutor());
        return result;
    }
}
