/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.table.miss.enforcer;

import java.util.Objects;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginMastershipChangeServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;

public class LLDPPacketPuntEnforcer implements AutoCloseable {
    private final MastershipChangeRegistration registration;

    public LLDPPacketPuntEnforcer(final OpenFlowPluginMastershipChangeServiceProvider mastershipChangeServiceProvider,
                                  final SalFlowService flowService) {
        registration = mastershipChangeServiceProvider
                .getMastershipChangeServiceManager()
                .register(new TableMissEnforcerMastershipChangeService(flowService));
    }

    @Override
    public void close() throws Exception {
        if (Objects.nonNull(registration)) {
            registration.close();
        }
    }
}
