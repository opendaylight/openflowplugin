/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import com.google.common.util.concurrent.CheckedFuture;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.translator.TranslatorLibrarian;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * This interface is responsible for instantiating DeviceContext and
 * registering transaction chain for each DeviceContext. Each device
 * has its own device context managed by this manager.
 */
public interface DeviceManager extends
        OFPManager,
        TranslatorLibrarian {

    /**
     * invoked after all services injected.
     */
    void initialize();

    void setFlowRemovedNotificationOn(boolean value);

    boolean isFlowRemovedNotificationOn();

    void setStatisticsPollingOn(boolean value);

    boolean isStatisticsPollingOn();

    void setGlobalNotificationQuota(long globalNotificationQuota);

    void setSwitchFeaturesMandatory(boolean switchFeaturesMandatory);

    void setSkipTableFeatures(boolean skipTableFeatures);

    void setBarrierCountLimit(int barrierCountLimit);

    void setBarrierInterval(long barrierTimeoutLimit);

    CheckedFuture<Void, TransactionCommitFailedException> removeDeviceFromOperationalDS(final KeyedInstanceIdentifier<Node, NodeKey> ii);

    DeviceContext createContext(@Nonnull final ConnectionContext connectionContext);

    void sendNodeAddedNotification(@Nonnull final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier);

    void sendNodeRemovedNotification(@Nonnull final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier);
}

