/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic.ofjava;

import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;

interface RpcRegistrationContext extends DeviceDisconnectedHandler {
    static <T extends RpcService> RpcRegistrationContext of(final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier,
                                                            final RpcProviderRegistry registry,
                                                            final Stream<ImmutablePair<Class<T>, T>> registrations) {
        return new RpcRegistrationContextImpl(instanceIdentifier, registry, registrations);
    }

    class RpcRegistrationContextImpl implements RpcRegistrationContext {
        private final Stream<RoutedRpcRegistration<?>> registrationStream;
        private final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier;

        <T extends RpcService> RpcRegistrationContextImpl(final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier,
                                                          final RpcProviderRegistry registry,
                                                          final Stream<ImmutablePair<Class<T>, T>> registrations) {
            this.instanceIdentifier = instanceIdentifier;
            registrationStream = registrations.map(r -> createRegistration(registry, r.getLeft(), r.getRight()));
        }

        private <T extends RpcService> RoutedRpcRegistration<T> createRegistration(final RpcProviderRegistry registry,
                                                                                   final Class<T> serviceInterface,
                                                                                   final T implementation) {
            final RoutedRpcRegistration<T> registration = registry.addRoutedRpcImplementation(serviceInterface, implementation);
            registration.registerPath(NodeContext.class, instanceIdentifier);
            return registration;
        }

        @Override
        public void onDeviceDisconnected(final ConnectionContext connectionContext) {
            registrationStream.forEach(routedRpcRegistration -> {
                routedRpcRegistration.unregisterPath(NodeContext.class, instanceIdentifier);
                routedRpcRegistration.close();
            });
        }
    }
}