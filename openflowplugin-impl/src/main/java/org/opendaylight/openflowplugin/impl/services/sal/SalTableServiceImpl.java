/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerTableMultipartService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerTableMultipartService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class SalTableServiceImpl implements SalTableService {

    private final SingleLayerTableMultipartService singleLayerService;
    private final MultiLayerTableMultipartService multiLayerService;

    public SalTableServiceImpl(final RequestContextStack requestContextStack,
                               final DeviceContext deviceContext,
                               final ConvertorExecutor convertorExecutor,
                               final MultipartWriterProvider multipartWriterProvider) {
        singleLayerService = new SingleLayerTableMultipartService(requestContextStack, deviceContext, convertorExecutor, multipartWriterProvider);
        multiLayerService = new MultiLayerTableMultipartService(requestContextStack, deviceContext, convertorExecutor, multipartWriterProvider);
    }

    @Override
    public Future<RpcResult<UpdateTableOutput>> updateTable(final UpdateTableInput input) {
        return singleLayerService.canUseSingleLayerSerialization()
            ? singleLayerService.handleAndReply(input)
            : multiLayerService.handleAndReply(input);
    }

}
