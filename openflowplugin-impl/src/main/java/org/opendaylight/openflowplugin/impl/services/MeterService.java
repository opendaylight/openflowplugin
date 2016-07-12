/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.MeterConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class MeterService<I extends Meter, O extends DataObject> extends AbstractSimpleService<I, O> {

    private final ConvertorManager convertorManager;
    private final VersionConvertorData data;

    MeterService(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final Class<O> clazz, final ConvertorManager convertorManager) {
        super(requestContextStack, deviceContext, clazz);
        this.convertorManager = convertorManager;
        data = new VersionConvertorData(getVersion());
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final I input) {
        final Optional<MeterModInputBuilder> ofMeterModInput = convertorManager.convert(input, data);
        final MeterModInputBuilder meterModInputBuilder = ofMeterModInput
                .orElse(MeterConvertor.defaultResult(getVersion()));

        meterModInputBuilder.setXid(xid.getValue());
        return meterModInputBuilder.build();
    }
}