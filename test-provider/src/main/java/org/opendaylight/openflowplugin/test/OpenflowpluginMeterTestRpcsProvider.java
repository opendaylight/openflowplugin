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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeter;
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
public final class OpenflowpluginMeterTestRpcsProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginMeterTestRpcsProvider.class);

    private final Registration meterRegistration;

    @Inject
    @Activate
    public OpenflowpluginMeterTestRpcsProvider(@Reference final RpcProviderService rpcRegistry) {
        meterRegistration = rpcRegistry.registerRpcImplementations(List.of(
            (RemoveMeter) input -> {
                LOG.info("removeMeter - {}", input);
                return null;
            },
            (AddMeter) input -> {
                LOG.info("addMeter - {}", input);
                return null;
            },
            (UpdateMeter) input -> {
                LOG.info("updateMeter - {}", input);
                return null;
            }), Set.of(InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID))).toIdentifier()));
        LOG.info("SalMeterRpcsProvider Started.");
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        meterRegistration.close();
        LOG.info("SalMeterRpcsProvide stopped.");
    }
}
