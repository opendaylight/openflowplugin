/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public abstract class AbstractMultipartOnTheFlyService<I> extends AbstractService<I, List<MultipartReply>> {
    private final ConvertorExecutor convertorExecutor;

    protected AbstractMultipartOnTheFlyService(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext);
        this.convertorExecutor = convertorExecutor;
    }

    @Override
    protected final FutureCallback<OfHeader> createCallback(final RequestContext<List<MultipartReply>> context, final Class<?> requestType) {
        return new MultipartRequestOnTheFlyCallback(context, requestType,
                getMessageSpy(), getEventIdentifier(), getDeviceInfo(),
                getDeviceContext().getDeviceFlowRegistry(), getTxFacade(),
                convertorExecutor);
    }


}
