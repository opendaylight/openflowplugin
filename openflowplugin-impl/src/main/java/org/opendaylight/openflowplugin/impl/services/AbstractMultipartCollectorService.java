/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMultipartCollectorService<T extends OfHeader> extends AbstractMultipartService<MultipartType, T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMultipartCollectorService.class);

    protected AbstractMultipartCollectorService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    protected FutureCallback<OfHeader> createCallback(RequestContext<List<T>> context, Class<?> requestType) {
        final FutureCallback<OfHeader> callback = super.createCallback(context, requestType);

        return new FutureCallback<OfHeader>() {
            @Override
            public void onSuccess(@Nullable final OfHeader result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(@Nonnull final Throwable t) {
                if (MultipartType.OFPMPTABLEFEATURES.getClass().equals(requestType)) {
                    makeEmptyTables(getDeviceContext().getPrimaryConnectionContext().getFeatures().getTables());
                }

                callback.onFailure(t);
            }
        };
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final MultipartType input) {
        return MultipartRequestInputFactory.makeMultipartRequestInput(xid.getValue(), getVersion(), input);
    }

    // FIXME : remove after ovs table features fix
    private void makeEmptyTables(final Short nrOfTables) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("About to create {} empty tables for node {}.", nrOfTables, getDeviceInfo().getLOGValue());
        }

        for (int i = 0; i < nrOfTables; i++) {
            final short tId = (short) i;
            final InstanceIdentifier<Table> tableII = getDeviceInfo()
                .getNodeInstanceIdentifier()
                .augmentation(FlowCapableNode.class).child(Table.class,
                    new TableKey(tId));
            final TableBuilder tableBuilder = new TableBuilder().setId(tId).addAugmentation(
                FlowTableStatisticsData.class, new FlowTableStatisticsDataBuilder().build());

            try {
                getTxFacade().writeToTransaction(LogicalDatastoreType.OPERATIONAL, tableII, tableBuilder.build());
            } catch (final Exception e) {
                LOG.debug("makeEmptyTables: Failed to write node {} to DS ", getDeviceInfo().getLOGValue(), e);
            }

        }
    }

}
