/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerMeterService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerMeterService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yangtools.binding.RpcOutput;

abstract class AbstractMeterRpc<I extends Meter, O extends RpcOutput> extends AbstractDeviceRpc {
    final @NonNull MultiLayerMeterService<I, O> multi;
    final @NonNull SingleLayerMeterService<O> single;

    AbstractMeterRpc(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor, final Class<O> output) {
        super(deviceContext);
        multi = new MultiLayerMeterService<>(requestContextStack, deviceContext, output, convertorExecutor);
        single = new SingleLayerMeterService<>(requestContextStack, deviceContext, output);
    }
}
