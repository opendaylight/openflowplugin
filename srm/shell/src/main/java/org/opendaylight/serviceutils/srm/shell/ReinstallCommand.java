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
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.ReinstallInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.ReinstallInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.ReinstallOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.SrmRpcsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.EntityNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.EntityTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.EntityTypeService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "srm", name = "reinstall", description = "Reinstall service or instance")
public class ReinstallCommand extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ReinstallCommand.class);

    private final SrmRpcsService srmRpcService;
    private final Class<? extends EntityTypeBase> entityType = EntityTypeService.class;

    public ReinstallCommand(SrmRpcsService srmRpcService) {
        this.srmRpcService = srmRpcService;
    }

    @Argument(index = 0, name = "name", description = "EntityName of type service, required",
        required = false, multiValued = false)
    String name;

    @Override
    protected @Nullable Object doExecute() throws Exception {
        ReinstallInput input = getInput();
        if (input == null) {
            // We've already shown the relevant error msg
            return null;
        }
        Future<RpcResult<ReinstallOutput>> result = srmRpcService.reinstall(input);
        RpcResult<ReinstallOutput> reinstallResult = result.get();
        printResult(reinstallResult);
        return null;
    }

    private void printResult(RpcResult<ReinstallOutput> reinstallResult) {
        StringBuilder strResult = new StringBuilder("");
        if (reinstallResult.isSuccessful()) {
            strResult.append("RPC call to reinstall was successful");
            LOG.trace("RPC Result: {}", reinstallResult.getResult());
        } else {
            strResult.append("RPC Call to reinstall failed.\n")
                .append("ErrorMsg: ").append(reinstallResult.getResult().getMessage());
            LOG.trace("RPC Result: {}", reinstallResult.getResult());
        }
        session.getConsole().println(strResult.toString());
    }

    private @Nullable ReinstallInput getInput() {
        Class<? extends EntityNameBase> entityName = SrmCliUtils.getEntityName(entityType, name);
        if (entityName == null) {
            session.getConsole().println(SrmCliUtils.getNameHelp(entityType));
            return null;
        }
        ReinstallInputBuilder inputBuilder = new ReinstallInputBuilder();
        inputBuilder.setEntityType(entityType);
        inputBuilder.setEntityName(entityName);
        return inputBuilder.build();
    }

}
