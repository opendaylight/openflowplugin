/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.singlelayer;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.OriginalGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;

public final class SingleLayerGroupService<O extends DataObject> extends AbstractSimpleService<Group, O> {

    public SingleLayerGroupService(
        final RequestContextStack requestContextStack,
        final DeviceContext deviceContext,
        final Class<O> clazz) {
        super(requestContextStack, deviceContext, clazz);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final Group input) throws ServiceException {
        final GroupMessageBuilder groupMessageBuilder = new GroupMessageBuilder(input);
        final Class<? extends DataContainer> clazz = input.getImplementedInterface();

        if (clazz.equals(AddGroupInput.class)) {
            groupMessageBuilder.setCommand(GroupModCommand.OFPGCADD);
        } else if (clazz.equals(UpdatedGroup.class)) {
            groupMessageBuilder.setCommand(GroupModCommand.OFPGCMODIFY);
        } else if (clazz.equals(RemoveGroupInput.class)
                || clazz.equals(OriginalGroup.class)) {
            groupMessageBuilder.setCommand(GroupModCommand.OFPGCDELETE);
        }

        return groupMessageBuilder
                .setVersion(getVersion())
                .setXid(xid.getValue())
                .build();
    }

}
