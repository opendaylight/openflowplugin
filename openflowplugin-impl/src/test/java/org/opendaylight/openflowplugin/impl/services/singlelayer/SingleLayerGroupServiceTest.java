/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.singlelayer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.Uint32;

public class SingleLayerGroupServiceTest extends ServiceMocking {
    private static final Uint32 GROUP_ID = Uint32.valueOf(42);
    private SingleLayerGroupService<AddGroupOutput> service;

    @Override
    protected void setup() {
        service = new SingleLayerGroupService<>(mockedRequestContextStack,
                mockedDeviceContext, AddGroupOutput.class);
    }

    @Test
    public void buildRequest() {
        final AddGroupInput input = new AddGroupInputBuilder()
                .setGroupId(new GroupId(GROUP_ID))
                .build();

        final OfHeader ofHeader = service.buildRequest(DUMMY_XID, input);
        assertEquals(GroupMessage.class, ofHeader.implementedInterface());

        final GroupMessage result = (GroupMessage) ofHeader;

        assertEquals(GroupModCommand.OFPGCADD, result.getCommand());
        assertEquals(GROUP_ID, result.getGroupId().getValue());
    }
}
