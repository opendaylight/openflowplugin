/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.multilayer;

import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.protocol.converter.GroupConverter;
import org.opendaylight.openflowplugin.protocol.converter.data.VersionDatapathIdConverterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;

public final class MultiLayerGroupService<I extends Group, O extends DataObject> extends AbstractSimpleService<I, O> {
    private final ConverterExecutor converterExecutor;
    private final VersionDatapathIdConverterData data;

    public MultiLayerGroupService(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final Class<O> clazz, final ConverterExecutor converterExecutor) {
        super(requestContextStack, deviceContext, clazz);
        this.converterExecutor = converterExecutor;
        data = new VersionDatapathIdConverterData(getVersion());
        data.setDatapathId(getDatapathId());
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final I input) throws ServiceException {
        final Optional<GroupModInputBuilder> ofGroupModInput = converterExecutor.convert(input, data);

        final GroupModInputBuilder groupModInputBuilder = ofGroupModInput
                .orElse(GroupConverter.defaultResult(getVersion()))
                .setXid(xid.getValue());

        return groupModInputBuilder.build();
    }
}
