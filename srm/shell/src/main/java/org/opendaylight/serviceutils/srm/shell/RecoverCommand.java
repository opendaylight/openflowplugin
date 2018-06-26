/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.serviceutils.srm.shell;

import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.OdlSrmRpcsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeBase;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "srm", name = "recover", description = "Recover service or instance")
public class RecoverCommand extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(RecoverCommand.class);

    private final OdlSrmRpcsService srmRpcService;

    public RecoverCommand(OdlSrmRpcsService srmRpcService) {
        this.srmRpcService = srmRpcService;
    }

    @Argument(index = 0, name = "type", description = "EntityType, required", required = false, multiValued = false)
    String type;

    @Argument(index = 1, name = "name", description = "EntityName, required", required = false, multiValued = false)
    String name;

    @Argument(index = 2, name = "id", description = "EntityId, optional", required = false, multiValued = false)
    String id;

    @Override
    protected @Nullable Object doExecute() throws Exception {
        RecoverInput input = getInput();
        if (input == null) {
            // We've already shown the relevant error msg
            return null;
        }
        Future<RpcResult<RecoverOutput>> result = srmRpcService.recover(input);
        RpcResult<RecoverOutput> recoverResult = result.get();
        printResult(recoverResult);
        return null;
    }

    private void printResult(RpcResult<RecoverOutput> recoverResult) {
        StringBuilder strResult = new StringBuilder("");
        if (recoverResult.isSuccessful()) {
            strResult.append("RPC call to recover was successful");
            LOG.trace("RPC Result: {}", recoverResult.getResult());
        } else {
            strResult.append("RPC Call to recover failed.\n")
                .append("ErrorCode: ").append(recoverResult.getResult().getResponse().getSimpleName())
                .append("ErrorMsg: ").append(recoverResult.getResult().getMessage());
            LOG.trace("RPC Result: {}", recoverResult.getResult());
        }
        session.getConsole().println(strResult.toString());
    }

    private @Nullable RecoverInput getInput() {
        if (type == null || name == null) {
            return null;
        }
        Class<? extends EntityTypeBase> entityType = SrmCliUtils.getEntityType(type);
        if (entityType == null) {
            session.getConsole().println(SrmCliUtils.getTypeHelp());
            return null;
        }
        Class<? extends EntityNameBase> entityName = SrmCliUtils.getEntityName(entityType, name);
        if (entityName == null) {
            session.getConsole().println(SrmCliUtils.getNameHelp(entityType));
            return null;
        }
        RecoverInputBuilder inputBuilder = new RecoverInputBuilder();
        inputBuilder.setEntityType(entityType);
        inputBuilder.setEntityName(entityName);
        if (id != null) {
            inputBuilder.setEntityId(id);
        }
        return inputBuilder.build();
    }

}
