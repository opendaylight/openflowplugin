/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

/**
 * @author Jozef Bacigal
 *         Date: 14.4.2016
 */
public interface RoleChangeListener {

    void roleInitializationDone(final NodeId nodeId, final boolean success);
    void roleCloseContext(final NodeId nodeId);
    void roleChangeOnDevice(final NodeId nodeId, final boolean success, final OfpRole newRole, final boolean initializationPhase);

}
