/**
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;

abstract class AbstractMessageService<R extends DataObject, I extends Builder<? extends R>, O extends DataObject>
        extends AbstractSimpleService<I, O>{

    protected AbstractMessageService(RequestContextStack requestContextStack, DeviceContext deviceContext, Class<O> clazz) {
        super(requestContextStack, deviceContext, clazz);
    }

    public abstract boolean isSupported();

    public abstract ListenableFuture<RpcResult<O>> processInput(R input);

}
