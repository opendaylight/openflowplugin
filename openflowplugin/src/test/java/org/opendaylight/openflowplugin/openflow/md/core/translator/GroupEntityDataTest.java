/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.reference.object.reference.GroupRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/10/14.
 */
public class GroupEntityDataTest {

    private static final GroupRef GROUP_REF = new GroupRef(InstanceIdentifier.create(Group.class));

    @Test
    public void trivalTest() {
        AddGroupInputBuilder addGroupInputBuilder = new AddGroupInputBuilder();
        addGroupInputBuilder.setGroupRef(GROUP_REF);
        GroupEntityData groupEntityData = new GroupEntityData();
        GroupRefBuilder groupRefBuilder = groupEntityData.getBuilder(addGroupInputBuilder.build());
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.reference.object.reference.GroupRef groupRef = groupRefBuilder.build();
        assertNotNull(groupRef.getGroupRef());

        UpdateGroupInputBuilder updateGroupInputBuilder = new UpdateGroupInputBuilder();
        updateGroupInputBuilder.setGroupRef(GROUP_REF);
        groupRefBuilder = groupEntityData.getBuilder(updateGroupInputBuilder.build());
        groupRef = groupRefBuilder.build();
        assertNotNull(groupRef.getGroupRef());

        RemoveGroupInputBuilder removeGroupInputBuilder = new RemoveGroupInputBuilder();
        removeGroupInputBuilder.setGroupRef(GROUP_REF);
        groupRefBuilder = groupEntityData.getBuilder(removeGroupInputBuilder.build());
        groupRef = groupRefBuilder.build();
        assertNotNull(groupRef.getGroupRef());

    }


}
