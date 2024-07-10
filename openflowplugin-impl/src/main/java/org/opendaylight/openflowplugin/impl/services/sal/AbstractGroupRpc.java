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
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerGroupService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerGroupService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yangtools.binding.RpcOutput;

abstract class AbstractGroupRpc<I extends Group, O extends RpcOutput> extends AbstractDeviceRpc {
    final @NonNull MultiLayerGroupService<I, O> multi;
    final @NonNull SingleLayerGroupService<O> single;

    AbstractGroupRpc(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor, final Class<O> output) {
        super(deviceContext);
        multi = new MultiLayerGroupService<>(requestContextStack, deviceContext, output, convertorExecutor);
        single = new SingleLayerGroupService<>(requestContextStack, deviceContext, output);
    }
}
