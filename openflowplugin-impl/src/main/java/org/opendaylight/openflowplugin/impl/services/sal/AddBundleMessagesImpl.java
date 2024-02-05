/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageSalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.bundle.add.message.sal.SalAddMessageDataBuilder;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public final class AddBundleMessagesImpl implements AddBundleMessages {
    private final SendExperimenter sendExperimenter;

    public AddBundleMessagesImpl(final SendExperimenter sendExperimenter) {
        this.sendExperimenter = requireNonNull(sendExperimenter);
    }

    @Override
    public ListenableFuture<RpcResult<AddBundleMessagesOutput>> invoke(final AddBundleMessagesInput input) {
        final var partialResults = new ArrayList<ListenableFuture<RpcResult<SendExperimenterOutput>>>();
        final var experimenterInputBuilder = new SendExperimenterInputBuilder()
            .setNode(input.getNode());
        final var bundleAddMessageBuilder = new BundleAddMessageSalBuilder();
        final var dataBuilder = new SalAddMessageDataBuilder()
            .setNode(input.getNode())
            .setBundleId(input.getBundleId())
            .setFlags(input.getFlags())
            .setBundleProperty(input.getBundleProperty());
        for (var message : input.nonnullMessages().getMessage()) {
            dataBuilder.setBundleInnerMessage(message.getBundleInnerMessage());
            experimenterInputBuilder.setExperimenterMessageOfChoice(
                    bundleAddMessageBuilder.setSalAddMessageData(dataBuilder.build()).build());
            partialResults.add(sendExperimenter.invoke(experimenterInputBuilder.build()));
        }

        final var result = SettableFuture.<RpcResult<AddBundleMessagesOutput>>create();
        Futures.addCallback(Futures.successfulAsList(partialResults),new FutureCallback<>() {
            @Override
            public void onSuccess(final List<RpcResult<SendExperimenterOutput>> results) {
                final var errors = new ArrayList<RpcError>();
                final RpcResultBuilder<AddBundleMessagesOutput> rpcResultBuilder;
                for (var res : results) {
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
                result.set(RpcResultBuilder.<AddBundleMessagesOutput>failed().build());
            }
        }, MoreExecutors.directExecutor());
        return result;
    }
}
