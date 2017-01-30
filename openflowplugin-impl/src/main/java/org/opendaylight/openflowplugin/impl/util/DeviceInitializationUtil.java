/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsDataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceInitializationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceInitializationUtil.class);

    private DeviceInitializationUtil() {
        // Hiding implicit constructor
    }

    // FIXME : remove after ovs table features fix
    public static void makeEmptyTables(final TxFacade txFacade, final DeviceInfo deviceInfo, final Short nrOfTables) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("About to create {} empty tables for node {}.", nrOfTables, deviceInfo.getLOGValue());
        }

        for (int i = 0; i < nrOfTables; i++) {
            try {
                txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL,
                    deviceInfo
                        .getNodeInstanceIdentifier()
                        .augmentation(FlowCapableNode.class)
                        .child(Table.class, new TableKey((short) i)),
                    new TableBuilder()
                        .setId((short) i)
                        .addAugmentation(
                            FlowTableStatisticsData.class,
                            new FlowTableStatisticsDataBuilder().build())
                        .build());
            } catch (final Exception e) {
                LOG.debug("makeEmptyTables: Failed to write node {} to DS ", deviceInfo.getLOGValue(), e);
            }
        };
    }

}
