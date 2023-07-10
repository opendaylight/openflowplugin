/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm.shell;

import java.util.concurrent.Future;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.OdlSrmRpcsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeService;
import org.opendaylight.yangtools.yang.common.RpcResult;
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
    private OdlSrmRpcsService srmRpcService;

    @Override
    public @Nullable Object execute() throws Exception {
        ReinstallInput input = getInput();
        if (input == null || srmRpcService == null) {
            return null;
        }
        Future<RpcResult<ReinstallOutput>> result = srmRpcService.reinstall(input);
        RpcResult<ReinstallOutput> reinstallResult = result.get();
        printResult(reinstallResult);
        return null;
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    private static void printResult(RpcResult<ReinstallOutput> reinstallResult) {
        var sb = new StringBuilder("");
        if (reinstallResult.isSuccessful()) {
            sb.append("RPC call to reinstall was successful");
            LOG.trace("RPC Result: {}", reinstallResult.getResult());
        } else {
            sb.append("RPC Call to reinstall failed.\n")
                .append("ErrorMsg: ").append(reinstallResult.getResult().getMessage());
            LOG.trace("RPC Result: {}", reinstallResult.getResult());
        }
        System.out.println(sb.toString());
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    private @Nullable ReinstallInput getInput() {
        EntityNameBase entityName = SrmCliUtils.getEntityName(ENTITY_TYPE, name);
        if (entityName == null) {
            System.out.println(SrmCliUtils.getNameHelp(ENTITY_TYPE));
            return null;
        }
        return new ReinstallInputBuilder()
            .setEntityType(ENTITY_TYPE)
            .setEntityName(entityName)
            .build();
    }
}
