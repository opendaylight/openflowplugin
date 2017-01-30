/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.TableUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract class AbstractTableMultipartService<T extends OfHeader> extends AbstractMultipartService<UpdateTableInput, T> {

    private final ConvertorExecutor convertorExecutor;
    private final MultipartWriterProvider multipartWriterProvider;
    private final VersionConvertorData data;

    protected AbstractTableMultipartService(final RequestContextStack requestContextStack,
                                            final DeviceContext deviceContext,
                                            final ConvertorExecutor convertorExecutor,
                                            final MultipartWriterProvider multipartWriterProvider) {
        super(requestContextStack, deviceContext);
        this.convertorExecutor = convertorExecutor;
        this.multipartWriterProvider = multipartWriterProvider;
        data = new VersionConvertorData(getVersion());
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final UpdateTableInput input) throws ServiceException {
        final Optional<List<TableFeatures>> tableFeatures = getConvertorExecutor().convert(input.getUpdatedTable(), data);

        return RequestInputUtils.createMultipartHeader(MultipartType.OFPMPTABLEFEATURES, xid.getValue(), getVersion())
            .setMultipartRequestBody(new MultipartRequestTableFeaturesCaseBuilder()
                .setMultipartRequestTableFeatures(new MultipartRequestTableFeaturesBuilder()
                    .setTableFeatures(tableFeatures
                        .orElseGet(Collections::emptyList))
                    .build())
                .build())
            .build();
    }

    /**
     * Get convertor executor
     * @return convertor executor
     */
    protected ConvertorExecutor getConvertorExecutor() {
        return convertorExecutor;
    }

    /**
     * Get data
     * @return data
     */
    protected VersionConvertorData getData() {
        return data;
    }

    /**
     * Stores table features to operational datastore
     */
    protected void storeStatistics(List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures> result) {
        multipartWriterProvider
            .lookup(MultipartType.OFPMPTABLEFEATURES)
            .ifPresent(writer -> {
                writer.write(
                    new TableUpdatedBuilder()
                        .setTableFeatures(result)
                        .build(),
                    false);

                getTxFacade().submitTransaction();
            });
    }

    /**
     * Process experimenter input and result experimenter output
     * @param input experimenter input
     * @return experimenter output
     */
    public abstract Future<RpcResult<UpdateTableOutput>> handleAndReply(UpdateTableInput input);

}
