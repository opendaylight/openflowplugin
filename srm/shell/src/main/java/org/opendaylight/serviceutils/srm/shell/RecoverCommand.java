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
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeBase;
import org.opendaylight.yangtools.yang.common.RpcResult;
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
    private OdlSrmRpcsService srmRpcService;

    @Override
    public @Nullable Object execute() throws Exception {
        RecoverInput input = getInput();
        if (input == null || srmRpcService == null) {
            // We've already shown the relevant error msg
            return null;
        }
        Future<RpcResult<RecoverOutput>> result = srmRpcService.recover(input);
        RpcResult<RecoverOutput> recoverResult = result.get();
        printResult(recoverResult);
        return null;
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    private static void printResult(RpcResult<RecoverOutput> recoverResult) {
        var sb = new StringBuilder("");
        if (recoverResult.isSuccessful()) {
            sb.append("RPC call to recover was successful");
            LOG.trace("RPC Result: {}", recoverResult.getResult());
        } else {
            sb.append("RPC Call to recover failed.\n")
                .append("ErrorCode: ").append(recoverResult.getResult().getResponse())
                .append("ErrorMsg: ").append(recoverResult.getResult().getMessage());
            LOG.trace("RPC Result: {}", recoverResult.getResult());
        }
        System.out.println(sb.toString());
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    private @Nullable RecoverInput getInput() {
        if (type == null || name == null) {
            return null;
        }
        EntityTypeBase entityType = SrmCliUtils.getEntityType(type);
        if (entityType == null) {
            System.out.println(SrmCliUtils.getTypeHelp());
            return null;
        }
        EntityNameBase entityName = SrmCliUtils.getEntityName(entityType, name);
        if (entityName == null) {
            System.out.println(SrmCliUtils.getNameHelp(entityType));
            return null;
        }
        var inputBuilder = new RecoverInputBuilder()
            .setEntityType(entityType)
            .setEntityName(entityName);
        if (id != null) {
            inputBuilder.setEntityId(id);
        }
        return inputBuilder.build();
    }
}
