/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractVoidService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.AsyncConfigConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInputBuilder;

public class SetAsyncConfigService extends AbstractVoidService<SetAsyncInput> {
    private final ConvertorExecutor convertorExecutor;
    private final VersionConvertorData data;

    public SetAsyncConfigService(RequestContextStack requestContextStack,
                                 DeviceContext deviceContext,
                                 ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext);
        this.convertorExecutor = convertorExecutor;
        data = new VersionConvertorData(getVersion());
    }

    @Override
    protected OfHeader buildRequest(Xid xid, SetAsyncInput input) throws ServiceException {
        final Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput>
                setAsyncInput = convertorExecutor.convert(input, data);

        final SetAsyncInputBuilder asyncInputBuilder = new SetAsyncInputBuilder(setAsyncInput
                .orElseGet(() -> AsyncConfigConvertor.defaultResult(getVersion())))
                .setVersion(getVersion())
                .setXid(xid.getValue());

        return asyncInputBuilder.build();
    }
}
