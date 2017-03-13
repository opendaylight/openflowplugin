/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.openflowplugin.impl.util.DeviceInitializationUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public abstract class AbstractMultipartCollectorService<T extends OfHeader> extends AbstractMultipartService<MultipartType, T> {

    protected AbstractMultipartCollectorService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    protected FutureCallback<OfHeader> createCallback(RequestContext<List<T>> context, Class<?> requestType) {
        final FutureCallback<OfHeader> callback = super.createCallback(context, requestType);

        return new FutureCallback<OfHeader>() {
            @Override
            public void onSuccess(@Nullable final OfHeader result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(@Nonnull final Throwable t) {
                // If we failed getting table features, at least create empty tables
                if (MultipartType.OFPMPTABLEFEATURES.getClass().equals(requestType)) {
                    DeviceInitializationUtil.makeEmptyTables(
                        getTxFacade(),
                        getDeviceInfo(),
                        getDeviceContext().getPrimaryConnectionContext().getFeatures().getTables());
                }

                callback.onFailure(t);
            }
        };
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final MultipartType input) {
        return MultipartRequestInputFactory.makeMultipartRequest(xid.getValue(), getVersion(), input, canUseSingleLayerSerialization());
    }
}
