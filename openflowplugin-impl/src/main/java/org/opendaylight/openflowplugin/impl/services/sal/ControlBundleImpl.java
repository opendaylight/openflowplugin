/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundle;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControlSalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.bundle.control.sal.SalControlDataBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ControlBundleImpl implements ControlBundle {
    private static final Logger LOG = LoggerFactory.getLogger(ControlBundleImpl.class);

    private final SendExperimenter sendExperimenter;

    public ControlBundleImpl(final SendExperimenter sendExperimenter) {
        this.sendExperimenter = requireNonNull(sendExperimenter);
    }

    @Override
    public ListenableFuture<RpcResult<ControlBundleOutput>> invoke(final ControlBundleInput input) {
        LOG.debug("Control message for device {} and bundle type {}", input.getNode(), input.getType());
        return Futures.transform(sendExperimenter.invoke(new SendExperimenterInputBuilder()
            .setNode(input.getNode())
            .setExperimenterMessageOfChoice(new BundleControlSalBuilder()
                .setSalControlData(new SalControlDataBuilder(input).build())
                .build())
            .build()), result ->
                result.isSuccessful() ? RpcResultBuilder.<ControlBundleOutput>success().build()
                    : RpcResultBuilder.<ControlBundleOutput>failed().build()
            , MoreExecutors.directExecutor());
    }
}
