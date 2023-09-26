/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm.shell;

import java.util.concurrent.ExecutionException;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.serviceutils.srm.spi.RegistryControl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation class of "srm:reinstall" Karaf shell command.
 */
@Service
@Command(scope = "srm", name = "reinstall", description = "Reinstall service or instance")
public class ReinstallCommand implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(ReinstallCommand.class);
    private static final EntityTypeBase ENTITY_TYPE = EntityTypeService.VALUE;

    @Argument(index = 0, name = "name", description = "EntityName of type service, required",
        required = false, multiValued = false)
    private String name;
    @Reference
    private RegistryControl control;

    @Override
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public Object execute() throws InterruptedException, ExecutionException {
        var entityName = SrmCliUtils.getEntityName(ENTITY_TYPE, name);
        if (entityName == null) {
            System.out.println(SrmCliUtils.getNameHelp(ENTITY_TYPE));
            return null;
        }

        if (control != null) {
            var output = control.reinstall(new ReinstallInputBuilder()
                .setEntityType(ENTITY_TYPE)
                .setEntityName(entityName)
                .build())
                .get();

            LOG.trace("RPC Result: {}", output);
            if (Boolean.TRUE.equals(output.getSuccessful())) {
                System.out.println("RPC call to reinstall was successful");
            } else {
                System.out.println("RPC Call to reinstall failed.");
                System.out.println("ErrorMsg: " + output.getMessage());
            }
        }
        return null;
    }
}
