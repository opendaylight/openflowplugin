/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy.STATISTIC_GROUP;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public final class VoidRequestCallback extends AbstractRequestCallback<Void> {
    private static final RpcResult<Void> SUCCESS = RpcResultBuilder.<Void>success().build();

    public VoidRequestCallback(final RequestContext<Void> context, final Class<?> requestType, final MessageSpy spy) {
        super(context, requestType, spy, null);
    }

    @Override
    public void onSuccess(final OfHeader result) {
        spyMessage(STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS_NO_RESPONSE);
        setResult(SUCCESS);
    }
}
