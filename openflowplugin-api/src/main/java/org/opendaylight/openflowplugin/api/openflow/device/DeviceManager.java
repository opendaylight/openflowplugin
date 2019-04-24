/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainHolder;
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

    ListenableFuture<?> removeDeviceFromOperationalDS(@NonNull KeyedInstanceIdentifier<Node, NodeKey> ii);

    DeviceContext createContext(@NonNull ConnectionContext connectionContext);


    void sendNodeAddedNotification(
            @NonNull KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier);

    void sendNodeRemovedNotification(
            @NonNull KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier);

    void setContextChainHolder(@NonNull ContextChainHolder contextChainHolder);
}

