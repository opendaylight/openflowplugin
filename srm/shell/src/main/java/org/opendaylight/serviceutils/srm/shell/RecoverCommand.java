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
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RpcSuccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation class of "srm:recover" Karaf shell command.
 */
@Service
@Command(scope = "srm", name = "recover", description = "Recover service or instance")
public class RecoverCommand implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(RecoverCommand.class);

    @Argument(index = 0, name = "type", description = "EntityType, required", required = false, multiValued = false)
    private String type;
    @Argument(index = 1, name = "name", description = "EntityName, required", required = false, multiValued = false)
    private String name;
    @Argument(index = 2, name = "id", description = "EntityId, optional", required = false, multiValued = false)
    private String id;
    @Reference
    private RegistryControl control;

    @Override
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public Object execute() throws InterruptedException, ExecutionException {
        if (type == null || name == null) {
            return null;
        }
        var entityType = SrmCliUtils.getEntityType(type);
        if (entityType == null) {
            System.out.println(SrmCliUtils.getTypeHelp());
            return null;
        }
        var entityName = SrmCliUtils.getEntityName(entityType, name);
        if (entityName == null) {
            System.out.println(SrmCliUtils.getNameHelp(entityType));
            return null;
        }

        if (control != null) {
            var inputBuilder = new RecoverInputBuilder()
                .setEntityType(entityType)
                .setEntityName(entityName);
            if (id != null) {
                inputBuilder.setEntityId(id);
            }
            var output = control.recover(inputBuilder.build())
                .get();

            LOG.trace("RPC Result: {}", output);
            var response = output.getResponse();
            if (RpcSuccess.VALUE.equals(response)) {
                System.out.println("RPC call to recover was successful");
            } else {
                System.out.println("RPC call to recover failed.");
                System.out.println("ErrorCode: " + response);
                System.out.println("ErrorMsg: " + output.getMessage());
            }
        }
        return null;
    }
}
