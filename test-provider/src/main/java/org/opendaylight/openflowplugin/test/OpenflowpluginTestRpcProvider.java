/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import java.util.List;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = { })
public final class OpenflowpluginTestRpcProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginTestRpcProvider.class);

    private final Registration flowRegistration;

    @Inject
    @Activate
    public OpenflowpluginTestRpcProvider(@Reference final RpcProviderService rpcRegistry) {
        flowRegistration = rpcRegistry.registerRpcImplementations(List.of(
            (AddFlow) input -> {
                LOG.info("addFlow - {}", input);
                return null;
            },
            (RemoveFlow) input -> {
                LOG.info("removeFlow - {}", input);
                return null;
            },
            (UpdateFlow) input -> {
                LOG.info("updateFlow - {}", input);
                return null;
            }), Set.of(InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID))).toIdentifier()));
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        LOG.info("SalFlowRpcProvide stopped.");
        flowRegistration.close();
    }
}
