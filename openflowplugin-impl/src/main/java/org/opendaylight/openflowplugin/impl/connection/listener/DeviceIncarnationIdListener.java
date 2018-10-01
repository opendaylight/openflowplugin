/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection.listener;

import java.math.BigInteger;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.DeviceIncarnationIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.device.incarnation.ids.DeviceIncarnationId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceIncarnationIdListener implements DataTreeChangeListener<DeviceIncarnationId>,
        AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceIncarnationIdListener.class);
    public final ConcurrentHashMap<BigInteger, DeviceIncarnationId> deviceIncarnationCache;
    private ListenerRegistration<DataTreeChangeListener> listenerRegistration;

    private final DataBroker dataBroker;

    public DeviceIncarnationIdListener(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.deviceIncarnationCache = new ConcurrentHashMap<>();
        registerListener();
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void registerListener() {
        InstanceIdentifier<DeviceIncarnationId> iiDeviceIncarnationId
                = InstanceIdentifier.create(DeviceIncarnationIds.class).child(DeviceIncarnationId.class);
        final DataTreeIdentifier<DeviceIncarnationId> treeId
                = new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, iiDeviceIncarnationId);
        try {
            listenerRegistration = dataBroker.registerDataTreeChangeListener(treeId, this);
        } catch (final Exception e) {
            LOG.warn("Device incarnation dataTreeChange listener registration fail");
            LOG.debug("Device incarnation dataTreeChange listener registration fail", e);
        }
    }

    @Override
    public void close() throws Exception {
        if (listenerRegistration != null) {
            listenerRegistration.close();
            listenerRegistration = null;
        }
    }

    @Override
    public void onDataTreeChanged(@Nonnull final Collection<DataTreeModification<DeviceIncarnationId>> modifications) {
        for (DataTreeModification modification : modifications) {
            switch (modification.getRootNode().getModificationType()) {
                case WRITE:
                    processIncarnationId(modification);
                    break;
                case SUBTREE_MODIFIED:
                    // NOOP
                    break;
                case DELETE:
                    // NOOP
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unhandled modification type: {}" + modification.getRootNode().getModificationType());
            }
        }
    }

    private void processIncarnationId(DataTreeModification<DeviceIncarnationId> modification) {
        DataObjectModification<DeviceIncarnationId> mod = modification.getRootNode();
        DeviceIncarnationId deviceIncarnationId = mod.getDataAfter();
        deviceIncarnationCache.put(deviceIncarnationId.getNodeId(), deviceIncarnationId);
    }

    public DeviceIncarnationId getDeviceIncarnationIdFromCache(BigInteger nodeId) {
        return deviceIncarnationCache.get(nodeId);
    }
}
