/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.registry.group;

import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 15.4.2015.
 */
public interface DeviceGroupRegistry extends AutoCloseable {

    void store(GroupId groupId);

    void markToBeremoved(GroupId groupId);

    void removeMarked();

    List<GroupId> getAllGroupIds();

    @Override
    void close();
}
