/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation define a contract between {@link Meter} data object
 * and {@link MeterAdded}, {@link MeterUpdated} and {@link MeterRemoved} notifications.
 */
public class MeterNotificationSupplierImpl extends
        AbstractNotifSupplierForItem<Meter, MeterAdded, MeterUpdated, MeterRemoved> {

    private static final InstanceIdentifier<Meter> wildCardedInstanceIdent = getNodeWildII().augmentation(FlowCapableNode.class).child(Meter.class);

    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationProviderService}
     * @param db - {@link DataBroker}
     */
    public MeterNotificationSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, Meter.class);
    }

    @Override
    public InstanceIdentifier<Meter> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public MeterAdded createNotification(final Meter o, final InstanceIdentifier<Meter> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);
        final MeterAddedBuilder builder = new MeterAddedBuilder(o);
        builder.setMeterRef(new MeterRef(path));
        builder.setNode(createNodeRef(path));
        return builder.build();
    }

    @Override
    public MeterUpdated updateNotification(final Meter o, final InstanceIdentifier<Meter> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);
        final MeterUpdatedBuilder builder = new MeterUpdatedBuilder(o);
        builder.setMeterRef(new MeterRef(path));
        builder.setNode(createNodeRef(path));
        return builder.build();
    }

    @Override
    public MeterRemoved deleteNotification(final InstanceIdentifier<Meter> path) {
        Preconditions.checkArgument(path != null);
        final MeterRemovedBuilder builder = new MeterRemovedBuilder();
        builder.setMeterId(path.firstKeyOf(Meter.class, MeterKey.class).getMeterId());
        builder.setMeterRef(new MeterRef(path));
        builder.setNode(createNodeRef(path));
        return builder.build();
    }
}

