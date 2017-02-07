/**
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.OriginalMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class MeterMessageService<O extends DataObject> extends AbstractMessageService<Meter, MeterMessageBuilder, O> {

    protected MeterMessageService(
            final RequestContextStack requestContextStack,
            final DeviceContext deviceContext,
            final Class<O> clazz) {
        super(requestContextStack, deviceContext, clazz);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final Meter input) throws ServiceException {
        final MeterMessageBuilder groupMessageBuilder = new MeterMessageBuilder(input);
        final Class<? extends DataContainer> clazz = input.getImplementedInterface();

        if (clazz.equals(AddMeterInput.class)
                || clazz.equals(UpdatedMeter.class)) {
            groupMessageBuilder.setCommand(MeterModCommand.OFPMCADD);
        } else if (clazz.equals(RemoveMeterInput.class)
                || clazz.equals(OriginalMeter.class)) {
            groupMessageBuilder.setCommand(MeterModCommand.OFPMCDELETE);
        }

        return groupMessageBuilder
                .setVersion(getVersion())
                .setXid(xid.getValue())
                .build();
    }

    @Override
    public boolean isSupported() {
        return super.isSupported() && getVersion() >= OFConstants.OFP_VERSION_1_3;
    }

}
